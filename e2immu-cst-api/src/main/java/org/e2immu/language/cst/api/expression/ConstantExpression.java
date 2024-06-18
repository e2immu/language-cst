package org.e2immu.language.cst.api.expression;

public interface ConstantExpression<T> extends Expression {

    @Override
    default boolean isConstant() {
        return true;
    }

    T constant();
}
