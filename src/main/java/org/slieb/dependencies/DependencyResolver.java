package org.slieb.dependencies;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class DependencyResolver<D extends DependencyNode<?>> {

    private final Collection<D> dependencyNodes;

    private final ArrayList<D> resolvedNodes;

    public DependencyResolver(Collection<D> dependencyNodes) {
        this.dependencyNodes = dependencyNodes;
        this.resolvedNodes = new ArrayList<D>();
    }

    public DependencyResolver(Collection<D> dependencyNodes, List<D> baseList) {
        this(dependencyNodes);
        if (baseList != null) {
            this.resolvedNodes.addAll(baseList);
        }
    }

    private D getProviderOfNamespace(String ns) {
        for (D node : dependencyNodes) {
            if (node.getProvides().contains(ns)) {
                return node;
            }
        }
        return null;
    }

    private void resolveDependencies(D node, ArrayList<D> dependencies, HashSet<D> parents) throws DependencyException {
        if (!dependencies.contains(node)) {
            parents.add(node);
            for (String namespace : node.getRequires()) {
                resolveDependencies(namespace, dependencies, parents);
            }
            parents.remove(node);
            dependencies.add(node);
        }
    }

    private void resolveDependencies(String namespace, ArrayList<D> dependencies, HashSet<D> parents) throws DependencyException {
        final D node = getProviderOfNamespace(namespace);

        if (node == null) {
            throw DependencyException.nothingProvides(namespace);
        }

        if (parents.contains(node)) {
            throw DependencyException.circularError(namespace, parents);
        }

        resolveDependencies(node, dependencies, parents);
    }


    public DependencyResolver<D> resolveNamespace(String namespace) throws DependencyException {
        resolveDependencies(namespace, resolvedNodes, new HashSet<D>());

        return this;
    }

    public DependencyResolver<D> resolveNamespaces(Collection<String> namespaces) throws DependencyException {
        for (String ns : namespaces) {
            resolveNamespace(ns);
        }

        return this;
    }

    public DependencyResolver<D> resolveNode(D node) throws DependencyException {
        resolveDependencies(node, resolvedNodes, new HashSet<D>());

        return this;
    }

    public DependencyResolver<D> resolveNodes(Collection<D> nodes) throws DependencyException {
        for (D node : nodes) {
            resolveNode(node);
        }
        return this;
    }

    public ImmutableList<D> resolve() {
        return ImmutableList.copyOf(resolvedNodes);
    }
}