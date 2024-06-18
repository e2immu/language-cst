package org.e2immu.cstapi.expression;

public interface EmptyExpression extends Expression {

    boolean isDefaultExpression();

    boolean isNoReturnValue();

    boolean isNoExpression();

    @Override
    default boolean isEmpty() {
        return true;
    }

    String msg();

}
