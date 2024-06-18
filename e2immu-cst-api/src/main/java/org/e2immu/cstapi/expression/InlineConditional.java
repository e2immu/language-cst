package org.e2immu.cstapi.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.runtime.Factory;

public interface InlineConditional extends Expression {
    Expression ifTrue();

    Expression ifFalse();

    Expression condition();

    interface Builder extends Element.Builder<Builder> {
        @Fluent
        Builder setIfTrue(Expression ifTrue);

        @Fluent
        Builder setIfFalse(Expression ifFalse);

        @Fluent
        Builder setCondition(Expression condition);

        InlineConditional build(Factory factory);
    }
}
