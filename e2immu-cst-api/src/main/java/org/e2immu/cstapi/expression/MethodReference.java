package org.e2immu.cstapi.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.type.ParameterizedType;

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
}
