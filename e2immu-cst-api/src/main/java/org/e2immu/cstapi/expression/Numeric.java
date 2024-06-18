package org.e2immu.cstapi.expression;

public interface Numeric extends Expression {
    Number number();

    double doubleValue();

    Expression negate();

    @Override
    default boolean isNegatedOrNumericNegative() {
        return doubleValue() < 0;
    }

    @Override
    default boolean isNumeric() {
        return true;
    }

    @Override
    default Double numericValue() {
        return doubleValue();
    }
}
