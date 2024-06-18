package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.info.MethodInfo;

import java.util.List;

public interface ExplicitConstructorInvocation extends Statement {

    boolean isSuper();

    MethodInfo methodInfo();

    List<Expression> parameterExpressions();

    interface Builder extends Statement.Builder<Builder> {
        @Fluent
        Builder setIsSuper(boolean isSuper);

        @Fluent
        Builder setMethodInfo(MethodInfo methodInfo);

        @Fluent
        Builder setParameterExpressions(List<Expression> parameterExpressions);

        ExplicitConstructorInvocation build();
    }
}
