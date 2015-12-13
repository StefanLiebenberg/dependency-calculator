package org.slieb.dependencies;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ModuleResolver<Node extends DependencyNode> {

    private final Set<String> seenModules;
    private final Map<String, Set<String>> moduleTree;
    private final Map<String, List<Node>> resolvedNodes;
    private final Map<Node, Set<Node>> dependencyMap;
    private final String commonModule;
    private final List<Node> baselist;


    public ModuleResolver(final Map<Node, Set<Node>> dependencyMap,
                          final String commonModule,
                          final List<Node> baselist) {
        this.dependencyMap = dependencyMap;
        this.seenModules = new HashSet<>();
        this.moduleTree = new ConcurrentHashMap<>();
        this.resolvedNodes = new ConcurrentHashMap<>();
        this.baselist = baselist;
        this.commonModule = commonModule;
    }


    public ModuleResolver(final Map<Node, Set<Node>> dependencyMap,
                          final String commonModule) {
        this(dependencyMap, commonModule, new ArrayList<>());
    }

    public ModuleResolver resolveModule(final String moduleName) {
        if (!this.seenModules.contains(moduleName)) {
            this.resolvedNodes.put(moduleName, new ArrayList<>());
            this.moduleTree.put(moduleName, new HashSet<>());
            this.seenModules.add(moduleName);
        }
        return this;
    }

    public ModuleResolver resolveModuleDependency(final String moduleName,
                                                  final String depModuleName) {
        this.resolveModule(moduleName);
        this.resolveModule(depModuleName);
        this.moduleTree.get(moduleName).add(depModuleName);
        return this;
    }


    public ModuleResolver<Node> resolveModuleWithNamespaces(final String moduleName,
                                                            final Set<String> namespaces) {
        namespaces.forEach(ns -> this.resolveModuleWithNamespace(moduleName, ns));
        return this;
    }

    public ModuleResolver<Node> resolveModuleWithNamespace(final String moduleName,
                                                           final String namespace) {

        if (moduleName == null) {
            throw DependencyException.cannotResolveNullModule();
        }

        if (namespace == null) {
            throw DependencyException.cannotResolveModuleWithNullNamespace(moduleName);
        }

        this.resolveModuleWithDependencyList(moduleName, new DependencyMapResolver<>(
                dependencyMap,
                Stream.concat(baselist.stream(),
                              getResolvedNodesForModule(moduleName))
                        .distinct()
                        .collect(Collectors.toList()))
                .resolveNode(getProviderOfNamespace(namespace).get())
                .resolve());
        return this;
    }

    private ModuleResolver<Node> resolveModuleWithDependencyList(final String moduleName,
                                                                 final List<Node> allModuleResolvedNodes) {
        this.resolveModule(moduleName);
        allModuleResolvedNodes.forEach(node -> {
            Set<String> modulesThatContainNode = this.getAllModulesFor(node);
            if (modulesThatContainNode.size() == 0) {
                this.resolvedNodes.get(moduleName).add(node);
            } else if (!(modulesThatContainNode.contains(moduleName) && modulesThatContainNode.size() == 1)) {
                final String commonModule = getCommonModule(
                        Stream.concat(modulesThatContainNode.stream(), Stream.of(moduleName)).distinct().collect(
                                toSet()));
                this.resolveModule(commonModule);
                if (modulesThatContainNode.contains(commonModule)) {
                    modulesThatContainNode.stream()
                            .filter(m -> !commonModule.equals(m))
                            .forEach(m -> {
                                this.resolvedNodes.get(m).remove(node);
                                this.moduleTree.get(m).add(commonModule);
                            });
                } else {
                    modulesThatContainNode.forEach(m -> {
                        this.resolvedNodes.get(m).remove(node);
                        this.moduleTree.get(m).add(commonModule);
                    });
                    this.resolvedNodes.get(commonModule).add(node);
                }
            }
        });
        return this;
    }

    private Stream<Node> getResolvedNodesForModule(String moduleName) {
        this.resolveModule(moduleName);
        return Stream.concat(this.moduleTree.get(moduleName).stream()
                                     .flatMap(depModule -> getResolvedNodesForModule(moduleName)),
                             this.resolvedNodes.get(moduleName).stream());
    }

    private Optional<Node> getProviderOfNamespace(String ns) {
        return dependencyMap.keySet().stream().filter(dep -> dep.getProvides().contains(ns)).findFirst();
    }

    private Set<String> getAllModulesFor(Node node) {
        return resolvedNodes.entrySet().stream()
                .filter(entry -> entry.getValue().contains(node)).map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }


    private String getCommonModule(Set<String> modulesWhereThisDepLives) {
        return modulesWhereThisDepLives.stream().reduce(this::getCommonOfTwoModules).orElse(this.commonModule);
    }


    private String getCommonOfTwoModules(final String moduleOne,
                                         final String moduleTwo) {
        return Optional.ofNullable(findCommonWithCursor(moduleOne, moduleTwo))
                .orElse(this.commonModule);
    }

    private String findCommonWithCursor(final String pivot,
                                        final String cursorStart) {
        String result = this.findCommonWithPivot(pivot, cursorStart);
        if (result != null && this.moduleTree.containsKey(pivot)) {
            result = this.moduleTree.get(pivot).stream()
                    .map(dep -> this.findCommonWithCursor(dep, cursorStart))
                    .findFirst().orElse(null);
        }
        return result;
    }

    private String findCommonWithPivot(final String pivot,
                                       final String cursor) {

        if (Objects.equals(pivot, cursor)) {
            return pivot;
        }

        return moduleTree.get(cursor).stream()
                .map(next -> findCommonWithPivot(pivot, next))
                .filter(r -> r != null)
                .findFirst().orElse(null);
    }

    public List<ModuleNode<Node>> resolve() {
        final Set<ModuleNode<Node>> modules = getModules();
        return new DependencyMapResolver<>(getModulesMap(modules))
                .resolveNodes(modules)
                .resolve();
    }

    public Map<ModuleNode<Node>, Set<ModuleNode<Node>>> getModulesMap(Set<ModuleNode<Node>> modules) {
        final Map<String, ModuleNode<Node>> provideMap =
                DependencyUtils.getProvideMap(modules, module -> Collections.singleton(module.getName()));
        return modules.stream().collect(toMap(Function.identity(), node -> {
            final Set<String> moduleDependencies = node.getModuleDependencies();
            return moduleDependencies.stream().map(provideMap::get).collect(
                    DependencyUtils.toSortedSet(ModuleNode::getName));
        }));
    }

    private ModuleNode<Node> createModuleNode(String name) {
        return new ModuleNode<>(name, moduleTree.get(name), resolvedNodes.get(name));
    }


    public Set<ModuleNode<Node>> getModules() {
        return seenModules.stream().map(this::createModuleNode).collect(
                DependencyUtils.toSortedSet(ModuleNode::getName));
    }
}
