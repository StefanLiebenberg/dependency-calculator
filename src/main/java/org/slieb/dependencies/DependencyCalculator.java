package org.slieb.dependencies;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public class DependencyCalculator<R, D extends DependencyNode<R>> {

    protected final Supplier<Iterable<R>> resourceSupplier;

    protected final DependencyParser<R, D> dependencyParser;

    protected final DependenciesHelper<D> dependenciesHelper;

    public DependencyCalculator(Supplier<Iterable<R>> provider, DependencyParser<R, D> parser, DependenciesHelper<D> helper) {
        this.resourceSupplier = provider;
        this.dependencyParser = parser;
        this.dependenciesHelper = helper;
    }

    public DependencyCalculator(Supplier<Iterable<R>> provider, DependencyParser<R, D> parser) {
        this.resourceSupplier = provider;
        this.dependencyParser = parser;
        this.dependenciesHelper = new DefaultDependencyHelper<>();
    }


    public ImmutableSet<D> getDependenciesSet() {
        ImmutableSet.Builder<D> setBuilder = new ImmutableSet.Builder<>();
        for (R r : resourceSupplier.get()) {
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
