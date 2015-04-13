package org.slieb.dependencies;

import com.google.common.collect.ImmutableSet;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class DependencyCalculator<R extends Resource.Readable, D extends DependencyNode<R>> {

    protected final ResourceProvider<R> resourceProvider;

    protected final DependencyParser<R, D> dependencyParser;

    protected final DependenciesHelper<D> dependenciesHelper;

    public DependencyCalculator(ResourceProvider<R> provider, DependencyParser<R, D> parser, DependenciesHelper<D> helper) {
        this.resourceProvider = provider;
        this.dependencyParser = parser;
        this.dependenciesHelper = helper;
    }

    public DependencyCalculator(ResourceProvider<R> provider, DependencyParser<R, D> parser) {
        this.resourceProvider = provider;
        this.dependencyParser = parser;
        this.dependenciesHelper = new DefaultDependencyHelper<>();
    }


    public ImmutableSet<D> getDependenciesSet() {
        ImmutableSet.Builder<D> setBuilder = new ImmutableSet.Builder<>();
        for (R r : resourceProvider.getResources()) {
            setBuilder.add(dependencyParser.parse(r));
        }
        return setBuilder.build();
    }

    public List<D> getDependenciesFor(Set<String> namespaces) {
        final ImmutableSet<D> deps = getDependenciesSet();
        return new DependencyResolver<D>(
                dependenciesHelper.getResolveableSet(deps),
                dependenciesHelper.getBaselist(deps))
                .resolveNamespaces(namespaces)
                .resolve();
    }

    public List<D> getDependenciesFor(String... namespaces) {
        return getDependenciesFor(ImmutableSet.copyOf(namespaces));
    }

    public List<R> getResourcesFor(Set<String> namespaces) {
        return getDependenciesFor(namespaces)
                .stream()
                .map(DependencyNode::getResource)
                .collect(toList());
    }

    public List<R> getResourcesFor(String... namespaces) {
        return getResourcesFor(ImmutableSet.copyOf(namespaces));
    }
}
