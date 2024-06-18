package org.e2immu.cstapi.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.type.Diamond;
import org.e2immu.cstapi.type.ParameterizedType;

import java.util.List;

public interface ConstructorCall extends Expression {
    MethodInfo constructor();

    Expression object();

    List<Expression> parameterExpressions();

    TypeInfo anonymousClass();

    ArrayInitializer arrayInitializer();

    ConstructorCall withParameterExpressions(List<Expression> newParameterExpressions);

    Diamond diamond();

    interface Builder extends Expression.Builder<Builder> {

        ConstructorCall build();

        @Fluent
        Builder setObject(Expression object);

        @Fluent
        Builder setDiamond(Diamond diamond);

        @Fluent
        Builder setConstructor(MethodInfo methodInfo);

        @Fluent
        Builder setAnonymousClass(TypeInfo anonymousClass);

        @Fluent
        Builder setArrayInitializer(ArrayInitializer arrayInitializer);

        @Fluent
        Builder setParameterExpressions(List<Expression> expressions);

        @Fluent
        Builder setConcreteReturnType(ParameterizedType returnType);
    }
}
