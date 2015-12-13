package org.slieb.dependencies;

import java.util.*;

/**
 * The dependency Resolver will resolve a collection of Dependencies.
 * note:          A draw back of this method is that too many items in the require chain will cause a long recursing
 * and result in a stackoverflow.
 *
 * @param <D> A extension of the dependency node.
 */
public class DependencyResolver<D extends DependencyNode<?>> {

    private final Collection<D> dependencyNodes;

    private final ArrayList<D> resolvedNodes;

    /**
     * @param dependencyNodes A list of dependency nodes..
     */
    public DependencyResolver(Collection<D> dependencyNodes) {
        this.dependencyNodes = dependencyNodes;
        this.resolvedNodes = new ArrayList<>();
    }

    /**
     * @param dependencyNodes A List of dependency nodes.
     * @param baseList        A baselist of files that need to be included.
     */
    public DependencyResolver(Collection<D> dependencyNodes,
                              List<D> baseList) {
        this(dependencyNodes);
        if (baseList != null) {
            this.resolvedNodes.addAll(baseList);
        }
    }

    private D getProviderOfNamespace(String ns) {
        return dependencyNodes.stream()
                .filter(n -> n.getProvides().contains(ns))
                .findFirst().orElse(null);
    }

    // synchronized because resolvedNodes add in here.
    private synchronized void resolveDependencies(D node,
                                                  HashSet<D> parents) throws DependencyException {
        if (!resolvedNodes.contains(node)) {
            parents.add(node);
            node.getRequires().forEach(ns -> resolveDependencies(ns, parents));
            parents.remove(node);
            resolvedNodes.add(node);
        }
    }

    private void resolveDependencies(String namespace, HashSet<D> parents) throws DependencyException {
        final D node = getProviderOfNamespace(namespace);

        if (node == null) {
            throw DependencyException.nothingProvides(namespace);
        }

        if (parents.contains(node)) {
            throw DependencyException.circularError(namespace, parents);
        }

        resolveDependencies(node, parents);
    }


    /**
     * @param namespace An additional namespaces to be resolved.
     * @return A reference to the dependency resolved.
     * @throws DependencyException If dependency resolution fails.
     */
    public DependencyResolver<D> resolveNamespace(String namespace) throws DependencyException {
        resolveDependencies(namespace, new HashSet<>());
        return this;
    }


    /**
     * @param namespaces Additional namespaces that need to be resolved.
     * @return A reference to the dependency resolved.
     * @throws DependencyException If dependency resolution fails.
     */
    public DependencyResolver<D> resolveNamespaces(Collection<String> namespaces) throws DependencyException {
        namespaces.forEach(this::resolveNamespace);
        return this;
    }


    /**
     * @param node Additional node who's dependencies need to be resolved.
     * @return A reference to the dependency resolved.
     * @throws DependencyException If dependency resolution fails.
     */
    public DependencyResolver<D> resolveNode(D node) throws DependencyException {
        resolveDependencies(node, new HashSet<>());
        return this;
    }

    /**
     * @param nodes Additional nodes who's dependencies need to be resolved.
     * @return A reference to the dependency resolver.
     * @throws DependencyException If dependency resolution fails.
     */
    public DependencyResolver<D> resolveNodes(Collection<D> nodes) throws DependencyException {
        nodes.forEach(this::resolveNode);
        return this;
    }

    /**
     * @return an {@link List} of dependency nodes.
     */
    public List<D> resolve() {
        return Collections.unmodifiableList(resolvedNodes);
    }
}