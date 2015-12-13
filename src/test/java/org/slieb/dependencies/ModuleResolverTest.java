package org.slieb.dependencies;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.slieb.dependencies.DependencyUtils.getDependencyMap;


public class ModuleResolverTest {

    List<DependencyNode> nodes;


    public DependencyNode mockNode(int i) {
        DependencyNode node = Mockito.mock(DependencyNode.class);
        when(node.getProvides()).thenReturn(Sets.newHashSet("node_" + i));
        return node;
    }

    @Before
    public void setUp() throws Exception {
        nodes = IntStream.range(0, 1000).mapToObj(this::mockNode).collect(Collectors.toList());
    }

    @Test
    public void testModuleResolverShouldAcceptDependencies() throws Exception {
        List<ModuleNode<DependencyNode>> result = new ModuleResolver<>(getDependencyMap(nodes), "common")
                .resolveModuleWithNamespace("packageA", "node_1")
                .resolve();
        ModuleNode node = result.get(0);
        assertEquals(Lists.newArrayList(nodes.get(1)), node.getNodes());
    }

    @Test
    public void testModuleResolverGroupCommonNodes() throws Exception {
        List<ModuleNode<DependencyNode>> result = new ModuleResolver<>(getDependencyMap(nodes), "common")
                .resolveModuleWithNamespace("packageA", "node_1")
                .resolveModuleWithNamespace("packageB", "node_1")
                .resolve();
        assertEquals(3, result.size());
        assertEquals("common", result.get(0).getName());
        assertEquals(Lists.newArrayList(nodes.get(1)), result.get(0).getNodes());
        assertEquals("packageA", result.get(1).getName());
        assertEquals(Lists.newArrayList(), result.get(1).getNodes());
        assertEquals("packageB", result.get(2).getName());
        assertEquals(Lists.newArrayList(), result.get(2).getNodes());
    }

    @Test
    public void testModuleResolverGroupCommonNodesAndLeaveUncommonOnes() throws Exception {

        List<ModuleNode<DependencyNode>> result = new ModuleResolver<>(getDependencyMap(nodes), "common")
                .resolveModuleWithNamespace("packageA", "node_1")
                .resolveModuleWithNamespace("packageA", "node_3")
                .resolveModuleWithNamespace("packageB", "node_1")
                .resolveModuleWithNamespace("packageB", "node_2").resolve();
        assertEquals(3, result.size());
        assertEquals("common", result.get(0).getName());
        assertEquals(Lists.newArrayList(nodes.get(1)), result.get(0).getNodes());
        assertEquals("packageA", result.get(1).getName());
        assertEquals(Lists.newArrayList(nodes.get(3)), result.get(1).getNodes());
        assertEquals("packageB", result.get(2).getName());
        assertEquals(Lists.newArrayList(nodes.get(2)), result.get(2).getNodes());
    }

    @Test
    public void testShouldResolveInternalRequirements() throws Exception {
        when(nodes.get(1).getRequires()).thenReturn(Sets.newHashSet("node_2"));
        when(nodes.get(2).getRequires()).thenReturn(Sets.newHashSet("node_3"));
        List<ModuleNode<DependencyNode>> result = new ModuleResolver<>(getDependencyMap(nodes), "common")
                .resolveModuleWithNamespace("packageA", "node_1")
                .resolve();
        assertEquals(1, result.size());
        assertEquals("packageA", result.get(0).getName());
        assertEquals(Lists.newArrayList(nodes.get(3), nodes.get(2), nodes.get(1)), result.get(0).getNodes());
    }

}