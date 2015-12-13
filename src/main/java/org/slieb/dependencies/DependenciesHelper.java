package org.slieb.dependencies;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Supplies additional information about the dependency resolution.
 *
 * @param <D>
 */
public interface DependenciesHelper<D extends DependencyNode> {

    /**
     * This produces a list of DependencyNodes that should always be included first, even if
     * the DependencyResolver does not list them.
     *
     * @param dependencies A Collection of dependencies
     * @return A List of dependencies that should appear in front of any other nodes.
     */
    default List<D> getBaseList(Collection<D> dependencies) {
        return Collections.unmodifiableList(Collections.emptyList());
    }

    /**
     * @param dependencies The dependency collection.
     * @return A set of dependencies that can be resolved.
     */
    default Set<D> getResolvableSet(Collection<D> dependencies) {
        return Collections.unmodifiableSet(dependencies.stream().collect(Collectors.toSet()));
    }

}

