package org.e2immu.language.cst.api.expression;

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
