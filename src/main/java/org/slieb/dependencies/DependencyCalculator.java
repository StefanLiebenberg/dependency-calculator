package org.slieb.dependencies;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class DependencyCalculator<R, D extends DependencyNode<R>> {

    protected final Iterable<R> resources;

    protected final DependencyParser<R, D> dependencyParser;

    protected final DependenciesHelper<D> dependenciesHelper;

    public DependencyCalculator(Iterable<R> resources,
                                DependencyParser<R, D> parser,
                                DependenciesHelper<D> helper) {
        this.resources = resources;
        this.dependencyParser = parser;
        this.dependenciesHelper = helper;
    }

    public DependencyCalculator(Iterable<R> resources,
                                DependencyParser<R, D> parser) {
        this(resources, parser, new DefaultHelper<>());
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
                dependenciesHelper.getResolvableSet(dependencies),
                dependenciesHelper.getBaseList(dependencies));
    }

    public List<D> getDependenciesFor(Set<String> namespaces) {
        return getDependencyResolver()
                .resolveNamespaces(namespaces)
                .resolve();
    }

    public List<D> getDependenciesFor(R resource) {
        if (resource == null) {
            throw DependencyException.cannotResolveNull();
        }
        return getDependencyResolver()
                .resolveNode(dependencyParser.parse(resource))
                .resolve();
    }

    public List<D> getDependenciesFor(String... namespaces) {
        return getDependenciesFor(immutableNamespaces(namespaces));
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
        return getResourcesFor(immutableNamespaces(namespaces));
    }

    private Set<String> immutableNamespaces(String... namespaces) {
        return Collections.unmodifiableSet(Arrays.stream(namespaces).collect(Collectors.toSet()));
    }

    private static class DefaultHelper<D extends DependencyNode> implements DependenciesHelper<D> {

    }
}
