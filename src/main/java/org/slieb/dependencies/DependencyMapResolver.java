package org.slieb.dependencies;

import java.util.*;


/**
 * The dependency Resolver will resolve a collection of Dependencies.
 */
public class DependencyMapResolver<Node> {

    private final Map<Node, Set<Node>> dependencyMap;

    private final List<Node> resolvedNodes;

    public DependencyMapResolver(Map<Node, Set<Node>> dependencyMap) {
        this.dependencyMap = dependencyMap;
        this.resolvedNodes = new ArrayList<>();
    }

    public DependencyMapResolver(Map<Node, Set<Node>> dependencyMap,
                                 List<Node> resolvedNodes) {
        this.dependencyMap = dependencyMap;
        this.resolvedNodes = new ArrayList<>(resolvedNodes);
    }

    // synchronized because resolvedNodes add in here.
    private synchronized void resolveDependencies(final Node node,
                                                  final HashSet<Node> parents) throws DependencyException {
        if (!resolvedNodes.contains(node)) {
            if (!dependencyMap.containsKey(node)) {
                throw DependencyException.nothingProvides(node);
            }
            parents.add(node);
            for (Node childNode : dependencyMap.get(node)) {
                if (parents.contains(childNode)) {
                    throw DependencyException.circularError(childNode, parents);
                }
                resolveDependencies(childNode, parents);
            }
            parents.remove(node);
            resolvedNodes.add(node);
        }
    }

    /**
     * @param node Additional node who's dependencies need to be resolved.
     * @return A reference to the dependency resolved.
     * @throws DependencyException If dependency resolution fails.
     */
    public DependencyMapResolver<Node> resolveNode(Node node) throws DependencyException {
        if (node == null) {
            throw DependencyException.cannotResolveDependenciesForNullResource();
        }
        try {
            resolveDependencies(node, new HashSet<>());
        } catch (DependencyException dependencyException) {
            throw DependencyException.cannotResolve(node, dependencyException);
        }
        return this;
    }

    /**
     * @param nodes Additional nodes who's dependencies need to be resolved.
     * @return A reference to the dependency resolver.
     * @throws DependencyException If dependency resolution fails.
     */
    public DependencyMapResolver<Node> resolveNodes(Collection<Node> nodes) throws DependencyException {
        if (nodes == null) {
            throw DependencyException.cannotResolveANullCollection();
        }
        nodes.forEach(this::resolveNode);
        return this;
    }

    /**
     * @return an {@link List} of dependency nodes.
     */
    public List<Node> resolve() {
        return Collections.unmodifiableList(resolvedNodes);
    }
}
