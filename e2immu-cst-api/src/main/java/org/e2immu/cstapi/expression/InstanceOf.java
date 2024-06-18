package org.e2immu.cstapi.expression;

import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.LocalVariable;

public interface InstanceOf extends Expression {
    Expression expression();

    LocalVariable patternVariable();

    ParameterizedType parameterizedType();
}
