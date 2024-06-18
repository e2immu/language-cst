package org.e2immu.cstapi.expression;

import org.e2immu.cstapi.info.MethodInfo;

public interface UnaryOperator extends Expression {
    Expression expression();

    MethodInfo operator();

    Precedence precedence();
}
