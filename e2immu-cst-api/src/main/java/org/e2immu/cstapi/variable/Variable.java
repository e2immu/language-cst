package org.e2immu.cstapi.variable;

import org.e2immu.annotation.NotNull;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.expression.util.OneVariable;
import org.e2immu.cstapi.type.ParameterizedType;

/**
 * It is important that we can sort variables, as part of the sorting system of Expressions.
 */
public interface Variable extends Comparable<Variable>, Element, OneVariable {
    @NotNull
    String fullyQualifiedName();

    @NotNull
    String simpleName();

    @NotNull
    ParameterizedType parameterizedType();

    boolean isLocal();

    @Override
    default int compareTo(Variable o) {
        throw new UnsupportedOperationException();
    }

    default boolean isStatic() {
        return false;
    }
}
