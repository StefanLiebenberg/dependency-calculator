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
        throw new DependencyException();
    }
}
