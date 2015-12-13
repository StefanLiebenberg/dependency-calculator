package org.slieb.dependencies;


import java.util.Collection;

public class DependencyException extends RuntimeException {

    public DependencyException(String s) {
        super(s);
    }

    public DependencyException(final String stringValue,
                               final DependencyException exception) {
        super(stringValue, exception);
    }

    public static DependencyException cannotResolveDependenciesForNullResource() {
        return new DependencyException("Cannot resolve dependencies for null resource");
    }

    public static <N, P> DependencyException circularError(final N ns,
                                                           final Collection<P> parents) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Circular dependency detected while trying to resolve \"%s\".\n", ns));
        builder.append("  Parents: \n");
        for (P node : parents) {
            builder.append("    + ").append(node).append("\n");
        }
        return new DependencyException(builder.toString());
    }

    public static <N> DependencyException nothingProvides(N node) {
        return new DependencyException(String.format("nothing provides %s", node.toString()));
    }

    public static DependencyException cannotResolveNullModule() {
        return new DependencyException("Cannot resolve module name of null.");
    }

    public static DependencyException cannotResolveModuleWithNullNamespace(String moduleName) {
        return new DependencyException(String.format("Cannot resolve namespace of null in module '%s'", moduleName));
    }

    public static DependencyException cannotResolveANullCollection() {
        return new DependencyException("Cannot resolve a null collection of nodes");
    }

    public static DependencyException cannotResolveNull() {
        return new DependencyException("The null value cannot be resolved.");
    }

    public static <Node> DependencyException cannotResolve(Node node,
                                                           DependencyException dependencyException) {
        return new DependencyException(String.format("Cannot resolve %s", node), dependencyException);
    }
}
