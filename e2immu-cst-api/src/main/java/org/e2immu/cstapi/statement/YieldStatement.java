package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.expression.Expression;

public interface YieldStatement extends Statement {

    interface Builder extends Statement.Builder<Builder> {
        @Fluent
        Builder setExpression(Expression expression);

        YieldStatement build();
    }
}
