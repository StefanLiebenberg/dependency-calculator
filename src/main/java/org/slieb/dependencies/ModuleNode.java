package org.slieb.dependencies;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class ModuleNode<Node extends DependencyNode> implements Serializable {

    private final String name;

    private final List<Node> nodes;

    private final Set<String> moduleDependencies;


    public ModuleNode(final String name,
                      final Set<String> moduleDependencies,
                      final List<Node> nodes) {
        this.name = name;
        this.nodes = nodes;
        this.moduleDependencies = moduleDependencies;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Set<String> getModuleDependencies() {
        return moduleDependencies;
    }

    public String getName() {
        return name;
    }
}
