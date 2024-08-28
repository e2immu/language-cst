package org.e2immu.language.cst.api.expression;


import org.e2immu.language.cst.api.type.ParameterizedType;

public interface ClassExpression extends ConstantExpression<ParameterizedType> {

    // String.class -> String
    ParameterizedType type();

    String NAME = "classLiteral";

    @Override
    default String name() {
        return NAME;
    }
}
