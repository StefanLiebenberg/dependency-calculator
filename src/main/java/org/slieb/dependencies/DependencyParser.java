package org.slieb.dependencies;

public interface DependencyParser<T, D extends DependencyNode<T>> {
    D parse(T resource);
}
