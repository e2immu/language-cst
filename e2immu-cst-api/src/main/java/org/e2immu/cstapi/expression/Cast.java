package org.e2immu.cstapi.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.type.ParameterizedType;

public interface Cast extends Expression {
    Expression expression();

    interface Builder extends Element.Builder<Builder> {
        @Fluent
        Builder setExpression(Expression expression);

        @Fluent
        Builder setParameterizedType(ParameterizedType parameterizedType);

        Cast build();
    }
}
