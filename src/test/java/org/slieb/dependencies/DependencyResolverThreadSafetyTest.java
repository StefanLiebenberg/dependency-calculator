package org.slieb.dependencies;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;


public class DependencyResolverThreadSafetyTest {

    @Test
    public void testThreadSafety() {
        DependencyResolver<Node> resolver =
                new DependencyResolver<>(
                        range(0, 1000)
                                .boxed()
                                .parallel()
                                .map(Node::create)
                                .collect(toSet()));
        range(100, 800)
                .boxed()
                .parallel()
                .map(String::valueOf)
                .forEach(resolver::resolveNamespace);
        List<Integer> result = resolver.resolve().stream().map(Node::getResource).collect(toList());
        assertEquals(800, result.size());
        range(0, 800).boxed().forEachOrdered(i -> assertEquals(i, result.get(i)));

    }

}

class Node implements DependencyNode<Integer> {

    public static Node create(Integer i) {
        return new Node(i,
                ImmutableSet.of(String.valueOf(i)),
                i == 0 ? ImmutableSet.of() : ImmutableSet.of(String.valueOf(i - 1)));
    }

    private final Integer resource;

    private final Set<String> provides, requires;

    public Node(Integer resource, Set<String> provides, Set<String> requires) {
        this.resource = resource;
        this.provides = provides;
        this.requires = requires;
    }

    @Override
    public Integer getResource() {
        return resource;
    }

    @Override
    public Set<String> getRequires() {
        return requires;
    }

    @Override
    public Set<String> getProvides() {
        return provides;
    }
}