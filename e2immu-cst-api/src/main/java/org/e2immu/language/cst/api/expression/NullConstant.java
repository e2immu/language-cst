package org.e2immu.language.cst.api.expression;

public interface NullConstant extends Expression {

    @Override
    default boolean isNullConstant() {
        return true;
    }
}
