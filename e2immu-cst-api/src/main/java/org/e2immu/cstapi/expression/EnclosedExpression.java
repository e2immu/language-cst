package org.e2immu.cstapi.expression;

public interface EnclosedExpression extends Expression {
    Expression inner();
}
