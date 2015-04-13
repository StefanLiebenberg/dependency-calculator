package org.slieb.dependencies;

import slieb.kute.api.Resource;


public interface DependencyParser<T extends Resource.Readable, D extends DependencyNode> {
    D parse(T resource);
}
