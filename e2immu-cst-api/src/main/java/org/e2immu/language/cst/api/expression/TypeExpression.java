package org.e2immu.language.cst.api.expression;

import org.e2immu.language.cst.api.type.ParameterizedType;

public interface TypeExpression extends Expression {
    ParameterizedType parameterizedType();

    String NAME = "typeExpression";

    @Override
    default String name() {
        return NAME;
    }
}
