package org.e2immu.language.cst.api.expression;

public interface Negation extends Expression {

    Expression expression();

    @Override
    default boolean isNegatedOrNumericNegative() {
        return true;
    }
}
