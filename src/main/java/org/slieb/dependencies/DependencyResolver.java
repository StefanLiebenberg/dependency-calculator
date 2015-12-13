package org.slieb.dependencies;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The dependency Resolver will resolve a collection of Dependencies.
 * note:          A draw back of this method is that too many items in the require chain will cause a long recursing
 * and result in a stackoverflow.
 *
 * @param <D> A extension of the dependency node.
 */
public class DependencyResolver<D extends DependencyNode<?>> {

    private final Map<String, D> provideMap;
    private final DependencyMapResolver<D> mapped;

    /**
     * @param dependencyNodes A list of dependency nodes..
     */
    public DependencyResolver(Collection<D> dependencyNodes) {
        this.provideMap = DependencyUtils.getProvideMap(dependencyNodes);
        this.mapped = new DependencyMapResolver<>(DependencyUtils.getDependencyMap(dependencyNodes, provideMap));
    }

    /**
     * @param dependencyNodes A List of dependency nodes.
     * @param baseList        A baselist of files that need to be included.
     */
    public DependencyResolver(Collection<D> dependencyNodes,
                              List<D> baseList) {
        this.provideMap = DependencyUtils.getProvideMap(dependencyNodes);
        this.mapped = new DependencyMapResolver<>(DependencyUtils.getDependencyMap(dependencyNodes, provideMap),
                                                  baseList);
    }


    /**
     * @param namespace An additional namespaces to be resolved.
     * @return A reference to the dependency resolved.
     * @throws DependencyException If dependency resolution fails.
     */
    public DependencyResolver<D> resolveNamespace(String namespace) throws DependencyException {
        this.mapped.resolveNode(
                Optional.ofNullable(this.provideMap.get(namespace))
                        .orElseThrow(() -> DependencyException.nothingProvides(namespace)));
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
        this.mapped.resolveNode(node);
        return this;
    }

    /**
     * @param nodes Additional nodes who's dependencies need to be resolved.
     * @return A reference to the dependency resolver.
     * @throws DependencyException If dependency resolution fails.
     */
    public DependencyResolver<D> resolveNodes(Collection<D> nodes) throws DependencyException {
        this.mapped.resolveNodes(nodes);
        return this;
    }

    /**
     * @return an {@link List} of dependency nodes.
     */
    public List<D> resolve() {
        return mapped.resolve();
    }
}