package org.slieb.dependencies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DependencyResolverTest {

    @Mock
    DependencyNode<?> depA, depB, depC;

    DependencyResolver<DependencyNode<?>> calculator;

    @Before
    public void setupResources() {
        when(depA.getProvides()).thenReturn(newHashSet("dep.a"));
        when(depB.getProvides()).thenReturn(newHashSet("dep.b"));
        when(depC.getProvides()).thenReturn(newHashSet("dep.c"));
        calculator = new DependencyResolver<>(ImmutableSet.of(depA, depB, depC));
    }

    @Test
    public void testDependencyResolutionTwoDeps() {
        when(depA.getRequires()).thenReturn(newHashSet("dep.b", "dep.c"));
        assertEquals(ImmutableList.of(depC, depB, depA), calculator.resolveNamespace("dep.a").resolve());
    }

    @Test
    public void testDependencyResolutionOneDep() {
        when(depA.getRequires()).thenReturn(newHashSet("dep.b"));
        assertEquals(ImmutableList.of(depB, depA), calculator.resolveNamespace("dep.a").resolve());
    }

    @Test
    public void testDepOrdering() {
        when(depA.getRequires()).thenReturn(newHashSet("dep.c"));
        when(depC.getRequires()).thenReturn(newHashSet("dep.b"));
        assertEquals(ImmutableList.of(depB, depC, depA), calculator.resolveNamespace("dep.a").resolve());
    }

    @Test(expected = DependencyException.class)
    public void testCircularError() {
        when(depA.getRequires()).thenReturn(newHashSet("dep.c"));
        when(depC.getRequires()).thenReturn(newHashSet("dep.b"));
        when(depC.getRequires()).thenReturn(newHashSet("dep.a"));
        calculator.resolveNamespace("dep.a").resolve();
    }

    @Test(expected = DependencyException.class)
    public void testNotFoundError() {
        when(depA.getRequires()).thenReturn(newHashSet("dep.c"));
        when(depC.getRequires()).thenReturn(newHashSet("dep.b"));
        when(depC.getRequires()).thenReturn(newHashSet("dep.d"));
        calculator.resolveNamespace("dep.a").resolve();
    }

}