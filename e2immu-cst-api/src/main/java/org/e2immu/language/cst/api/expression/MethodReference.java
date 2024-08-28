package org.e2immu.language.cst.api.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.type.ParameterizedType;

public interface MethodReference extends Expression {
    Expression scope();

    MethodInfo methodInfo();

    interface Builder extends Expression.Builder<Builder> {
        @Fluent
        Builder setScope(Expression expression);

        @Fluent
        Builder setMethod(MethodInfo method);

        @Fluent
        Builder setConcreteReturnType(ParameterizedType parameterizedType);

        MethodReference build();
    }

    String NAME = "methodReference";

    @Override
    default String name() {
        return NAME;
    }
}
