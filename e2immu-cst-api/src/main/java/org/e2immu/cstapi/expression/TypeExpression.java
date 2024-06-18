package org.e2immu.cstapi.expression;

import org.e2immu.cstapi.type.ParameterizedType;

public interface TypeExpression extends Expression {
    ParameterizedType parameterizedType();
}
