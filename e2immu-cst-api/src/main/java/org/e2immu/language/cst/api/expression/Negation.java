package org.e2immu.language.cst.api.expression;

public interface Negation extends Expression, ExpressionWrapper {

    @Override
    default boolean isNegatedOrNumericNegative() {
        return true;
    }
}
