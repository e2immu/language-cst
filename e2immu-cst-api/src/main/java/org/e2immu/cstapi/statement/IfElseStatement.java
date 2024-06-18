package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.expression.Expression;

public interface IfElseStatement extends Statement {

    Block elseBlock();

    interface Builder extends Statement.Builder<Builder> {
        @Fluent
        Builder setExpression(Expression expression);

        @Fluent
        Builder setIfBlock(Block ifBlock);

        @Fluent
        Builder setElseBlock(Block ifBlock);

        IfElseStatement build();
    }
}
