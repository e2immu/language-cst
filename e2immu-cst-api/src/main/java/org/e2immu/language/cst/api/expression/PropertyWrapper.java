package org.e2immu.language.cst.api.expression;

import org.e2immu.language.cst.api.type.ParameterizedType;

public interface PropertyWrapper extends Expression {
    ParameterizedType castType();

    Expression getExpression();
}
