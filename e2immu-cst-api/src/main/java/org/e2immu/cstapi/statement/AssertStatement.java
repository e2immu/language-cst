package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.expression.Expression;

public interface AssertStatement extends Statement {

    Expression message();

    interface Builder extends Statement.Builder<Builder> {
        @Fluent
        Builder setExpression(Expression expression);

        @Fluent
        Builder setMessage(Expression message);

        AssertStatement build();
    }
}
