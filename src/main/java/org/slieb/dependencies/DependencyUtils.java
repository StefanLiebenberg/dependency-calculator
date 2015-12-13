package org.slieb.dependencies;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class DependencyUtils {


    public static <Key, Node> Map<Key, Node> getProvideMap(final Collection<Node> nodes,
                                                           final Function<Node, Set<Key>> keyMapFunction) {
        final Map<Key, Node> provideMap = new HashMap<>();
        nodes.forEach(node -> {
            keyMapFunction.apply(node)
                    .stream()
                    .forEach(key -> {
                        provideMap.put(key, node);
                    });
        });
        return Collections.unmodifiableMap(provideMap);
    }


    public static <D extends DependencyNode<?>> Map<String, D> getProvideMap(Collection<D> nodes) {
        return getProvideMap(nodes, DependencyNode::getProvides);
    }


    public static <D extends DependencyNode<?>> Map<D, Set<D>> getDependencyMap(Collection<D> nodes) {
        final Map<String, D> provideMap = getProvideMap(nodes);
        return getDependencyMap(nodes, provideMap);
    }


    public static <Node, C extends Collection<Node>> Map<Node, C> getDependencyMap(
            final Collection<Node> nodes,
            final Function<Node, C> requiresMethod) {
        return Collections.unmodifiableMap(nodes.stream().collect(toMap(Function.identity(), requiresMethod)));
    }

    public static <D extends DependencyNode<?>> Map<D, Set<D>> getDependencyMap(Collection<D> nodes,
                                                                                Map<String, D> provideMap) {
        return getDependencyMap(nodes, (node) ->
                node.getRequires().stream()
                        .map(require -> {
                            if (provideMap.containsKey(require)) {
                                return provideMap.get(require);
                            } else {
                                throw DependencyException.nothingProvides(require);
                            }
                        })
                        .distinct()
                        .collect(Collectors.toSet()));
    }

    public static <N, U extends Comparable<? super U>> Collector<N, ?, Set<N>> toSortedSet(
            final Function<N, U> byProperty) {
        return Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(byProperty)));

    }
}
