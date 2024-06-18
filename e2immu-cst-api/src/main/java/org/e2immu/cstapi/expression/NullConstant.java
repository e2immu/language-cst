package org.e2immu.cstapi.expression;

public interface NullConstant extends Expression {

    @Override
    default boolean isNullConstant() {
        return true;
    }
}
