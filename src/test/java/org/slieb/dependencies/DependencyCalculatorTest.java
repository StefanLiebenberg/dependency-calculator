package org.slieb.dependencies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DependencyCalculatorTest {

    public static class R {
    }


    String nsA = "ns.A", nsB = "ns.B", nsC = "ns.c", nsD = "ns.d";

    @Mock
    R resourceA, resourceB, resourceC, resourceD;

    @Mock
    DependencyNode<R> depA, depB, depC, depD;


    @Mock
    DependencyParser<R, DependencyNode<R>> mockParser;


    DependencyCalculator<R, DependencyNode<R>> calculator;

    private void setupReadable(R resource,
                               DependencyNode<R> dependency,
                               String ns) {
        when(mockParser.parse(resource)).thenReturn(dependency);
        when(dependency.getResource()).thenReturn(resource);
        when(dependency.getProvides()).thenReturn(ImmutableSet.of(ns));
    }

    @Before
    public void setup() {
        setupReadable(resourceA, depA, nsA);
        setupReadable(resourceB, depB, nsB);
        setupReadable(resourceC, depC, nsC);
        setupReadable(resourceD, depD, nsD);
        calculator = new DependencyCalculator<>(ImmutableSet.of(resourceA, resourceB, resourceC, resourceD),
                                                mockParser);
    }


    @Test
    public void testGetDependenciesFor() {
        when(depA.getRequires()).thenReturn(ImmutableSet.of(nsB, nsC, nsD));
        when(depB.getRequires()).thenReturn(ImmutableSet.of(nsC, nsD));
        when(depC.getRequires()).thenReturn(ImmutableSet.of(nsD));
        when(depD.getRequires()).thenReturn(ImmutableSet.of());

        assertEquals(ImmutableList.of(depD, depC, depB, depA), calculator.getDependenciesFor(nsA));
        assertEquals(ImmutableList.of(depD, depC, depB), calculator.getDependenciesFor(nsB));
        assertEquals(ImmutableList.of(depD, depC), calculator.getDependenciesFor(nsC));
        assertEquals(ImmutableList.of(depD), calculator.getDependenciesFor(nsD));
    }

    
    @Test(expected = DependencyException.class)
    public void testGetCircularResource() {
        when(depA.getRequires()).thenReturn(ImmutableSet.of(nsA));
        calculator.getResourcesFor(nsA);
    }

    @Test(expected = DependencyException.class)
    public void testNotFound() {
        calculator.getResourcesFor("ns.X");
    }


    @Test
    public void testGetResourcesForResource() {
        when(depA.getRequires()).thenReturn(ImmutableSet.of(nsB, nsC, nsD));
        when(depB.getRequires()).thenReturn(ImmutableSet.of(nsC, nsD));
        when(depC.getRequires()).thenReturn(ImmutableSet.of(nsD));
        assertEquals(ImmutableList.of(resourceD, resourceC, resourceB, resourceA),
                     calculator.getResourcesFor(resourceA));
        assertEquals(ImmutableList.of(resourceD, resourceC, resourceB), calculator.getResourcesFor(resourceB));
        assertEquals(ImmutableList.of(resourceD, resourceC), calculator.getResourcesFor(resourceC));
        assertEquals(ImmutableList.of(resourceD), calculator.getResourcesFor(resourceD));
    }
}