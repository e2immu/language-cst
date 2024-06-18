package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.expression.Expression;

import java.util.List;

public interface ForStatement extends LoopStatement {
    // can be either a LocalVariableCreation (Statement) or any expression
    List<Element> initializers();

    List<Expression> updaters();

    interface Builder extends Statement.Builder<Builder> {

        @Fluent
        Builder addInitializer(Element initializer);

        @Fluent
        Builder setExpression(Expression expression);

        @Fluent
        Builder addUpdater(Expression expression);

        @Fluent
        Builder setBlock(Block block);

        ForStatement build();
    }
}
