package org.slieb.dependencies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;

public class DefaultDependencyHelper<D extends DependencyNode<?>> implements DependenciesHelper<D> {

    public ImmutableList<D> getBaselist(Collection<D> dependencies) {
        return ImmutableList.of();
    }

    public ImmutableSet<D> getResolveableSet(Collection<D> dependencies) {
        return ImmutableSet.copyOf(dependencies);
    }
}
