package org.slieb.dependencies;


import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DependenciesHelper<D extends DependencyNode> {

    List<D> getBaselist(Collection<D> dependencies);

    Set<D> getResolveableSet(Collection<D> dependencies);

}

