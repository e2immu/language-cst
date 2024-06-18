package org.e2immu.cstapi.expression;


import org.e2immu.annotation.Fluent;

import java.util.List;

public interface CommaExpression extends Expression {

    List<Expression> expressions();

    interface Builder extends Expression.Builder<Builder> {
        @Fluent
        Builder addExpression(Expression expression);

        @Fluent
        Builder addExpressions(List<Expression> expressions);

        CommaExpression build();
    }
}
