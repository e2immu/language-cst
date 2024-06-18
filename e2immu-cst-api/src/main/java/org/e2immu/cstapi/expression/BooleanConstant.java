package org.e2immu.cstapi.expression;

public interface BooleanConstant extends ConstantExpression<Boolean> {

    BooleanConstant negate();

    @Override
    default boolean isBooleanConstant() {
        return true;
    }

    @Override
    default boolean isBoolValueFalse() {
        return !constant();
    }

    @Override
    default boolean isBoolValueTrue() {
        return constant();
    }
}
