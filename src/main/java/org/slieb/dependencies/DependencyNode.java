package org.slieb.dependencies;


import java.util.Set;

public interface DependencyNode<R> {

    R getResource();

    Set<String> getRequires();

    Set<String> getProvides();
}
