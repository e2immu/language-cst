package org.e2immu.cstapi.variable;

public interface DescendMode {
    boolean isYes(); // all variables, recursively
    boolean isNo(); // all variables, no recursive descend
}
