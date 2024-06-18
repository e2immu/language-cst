package org.e2immu.cstapi.expression;

import org.e2immu.cstapi.type.ParameterizedType;

public interface PropertyWrapper extends Expression {
    ParameterizedType castType();

    Expression getExpression();
}
