package org.e2immu.cstapi.expression;

public interface ConstantExpression<T> extends Expression {

    @Override
    default boolean isConstant() {
        return true;
    }

    T constant();
}
