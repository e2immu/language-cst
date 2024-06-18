package org.e2immu.language.cst.api.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.type.Diamond;
import org.e2immu.language.cst.api.type.ParameterizedType;

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
