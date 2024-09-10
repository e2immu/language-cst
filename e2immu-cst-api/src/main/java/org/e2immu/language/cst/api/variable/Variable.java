package org.e2immu.language.cst.api.variable;

import org.e2immu.annotation.NotNull;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.expression.util.OneVariable;
import org.e2immu.language.cst.api.type.ParameterizedType;

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

    @Override
    default int compareTo(Variable o) {
       return fullyQualifiedName().compareTo(o.fullyQualifiedName());
    }

    default boolean isStatic() {
        return false;
    }
}
