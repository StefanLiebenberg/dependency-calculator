package org.slieb.dependencies;


import java.util.Set;

public class DependencyException extends RuntimeException {
    public DependencyException() {
    }

    public DependencyException(String s) {
        super(s);
    }

    public DependencyException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DependencyException(Throwable throwable) {
        super(throwable);
    }

    public DependencyException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

    public static DependencyException nothingProvides(String missingProvide) {
        throw new DependencyException(String.format("nothing provides %s", missingProvide));
    }

    public static DependencyException circularError(String ns, Set<? extends DependencyNode> parents) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Circular dependency detected while trying to resolve \"%s\".\n", ns));
        builder.append("  Parents: \n");
        for (DependencyNode<?> node : parents) {
            builder.append("    + ").append(node.getResource()).append("\n");
            for (String provide : node.getProvides()) {
                builder.append("        \"").append(provide).append("\"\n");
            }
        }
        throw new DependencyException(builder.toString());
    }
}
