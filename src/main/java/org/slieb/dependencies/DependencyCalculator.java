package org.slieb.dependencies;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class DependencyCalculator<R, D extends DependencyNode<R>> {

    protected final Iterable<R> resources;

    protected final DependencyParser<R, D> dependencyParser;

    protected final DependenciesHelper<D> dependenciesHelper;

    public DependencyCalculator(Iterable<R> resources, DependencyParser<R, D> parser, DependenciesHelper<D> helper) {
        this.resources = resources;
        this.dependencyParser = parser;
        this.dependenciesHelper = helper;
    }

    public DependencyCalculator(Iterable<R> resources, DependencyParser<R, D> parser) {
        this(resources, parser, new DefaultDependencyHelper<>());
    }


    private Stream<R> resourceStream() {
        if (resources instanceof Collection) {
            return ((Collection<R>) resources).stream();
        } else {
            return StreamSupport.stream(resources.spliterator(), false);
        }
    }

    public Collection<D> getDependencyNodes() {
        return resourceStream().map(dependencyParser::parse).collect(toSet());
    }

    public DependencyResolver<D> getDependencyResolver() {
        Collection<D> dependencies = getDependencyNodes();
        return new DependencyResolver<>(
                dependenciesHelper.getResolveableSet(dependencies),
                dependenciesHelper.getBaselist(dependencies));
    }

    public List<D> getDependenciesFor(Set<String> namespaces) {
        return getDependencyResolver()
                .resolveNamespaces(namespaces)
                .resolve();
    }

    public List<D> getDependenciesFor(R resource) {
        Preconditions.checkNotNull(resource, "Given Resource cannot be null");
        return getDependencyResolver()
                .resolveNode(dependencyParser.parse(resource))
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

    public List<R> getResourcesFor(R resource) {
        return getDependenciesFor(resource)
                .stream()
                .map(DependencyNode::getResource)
                .collect(toList());
    }


    public List<R> getResourcesFor(String... namespaces) {
        return getResourcesFor(ImmutableSet.copyOf(namespaces));
    }
}
