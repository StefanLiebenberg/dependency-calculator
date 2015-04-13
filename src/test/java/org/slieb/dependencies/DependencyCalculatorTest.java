package org.slieb.dependencies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DependencyCalculatorTest {


    String nsA = "ns.A", nsB = "ns.B", nsC = "ns.c", nsD = "ns.d";

    @Mock
    Resource.Readable resourceA, resourceB, resourceC, resourceD;

    @Mock
    DependencyNode<Resource.Readable> depA, depB, depC, depD;

    @Mock
    ResourceProvider<Resource.Readable> mockProvider;

    @Mock
    DependencyParser<Resource.Readable, DependencyNode<Resource.Readable>> mockParser;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    DefaultDependencyHelper<DependencyNode<Resource.Readable>> mockHelper;

    DependencyCalculator<Resource.Readable, DependencyNode<Resource.Readable>> calculator;


    private void setupReadable(Resource.Readable resource, DependencyNode<Resource.Readable> dependency, String ns) {
        when(mockParser.parse(resource)).thenReturn(dependency);
        when(dependency.getResource()).thenReturn(resource);
        when(dependency.getProvides()).thenReturn(ImmutableSet.of(ns));
    }

    @Before
    public void setup() {

        when(mockProvider.getResources()).thenReturn(ImmutableSet.of(resourceA, resourceB, resourceC, resourceD));
        setupReadable(resourceA, depA, nsA);
        setupReadable(resourceB, depB, nsB);
        setupReadable(resourceC, depC, nsC);
        setupReadable(resourceD, depD, nsD);

        calculator = new DependencyCalculator<>(mockProvider, mockParser, mockHelper);
    }

    @Test
    public void testGetDependenciesSet() {
        assertEquals(ImmutableSet.of(depA, depB, depC, depD), calculator.getDependenciesSet());
    }

    @Test
    public void testGetDependenciesFor() {
        when(depA.getRequires()).thenReturn(ImmutableSet.of(nsB, nsC, nsD));
        when(depB.getRequires()).thenReturn(ImmutableSet.of(nsC, nsD));
        when(depC.getRequires()).thenReturn(ImmutableSet.of(nsD));

        assertEquals(ImmutableList.of(depD, depC, depB, depA), calculator.getDependenciesFor(nsA));
        assertEquals(ImmutableList.of(depD, depC, depB), calculator.getDependenciesFor(nsB));
        assertEquals(ImmutableList.of(depD, depC), calculator.getDependenciesFor(nsC));
        assertEquals(ImmutableList.of(depD), calculator.getDependenciesFor(nsD));
    }

    @Test
    public void testGetResourcesFor() {
        when(depA.getRequires()).thenReturn(ImmutableSet.of(nsB, nsC, nsD));
        when(depB.getRequires()).thenReturn(ImmutableSet.of(nsC, nsD));
        when(depC.getRequires()).thenReturn(ImmutableSet.of(nsD));

        assertEquals(ImmutableList.of(resourceD, resourceC, resourceB, resourceA), calculator.getResourcesFor(nsA));
        assertEquals(ImmutableList.of(resourceD, resourceC, resourceB), calculator.getResourcesFor(nsB));
        assertEquals(ImmutableList.of(resourceD, resourceC), calculator.getResourcesFor(nsC));
        assertEquals(ImmutableList.of(resourceD), calculator.getResourcesFor(nsD));
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

}