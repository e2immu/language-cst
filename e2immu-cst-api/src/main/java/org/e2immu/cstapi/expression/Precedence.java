package org.e2immu.cstapi.expression;

public interface Precedence {
    int value();

    default boolean greaterThan(Precedence other) {
        return value() > other.value();
    }
}
