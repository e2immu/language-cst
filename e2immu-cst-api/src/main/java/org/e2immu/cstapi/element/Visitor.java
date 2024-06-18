package org.e2immu.cstapi.element;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.statement.Statement;
import org.e2immu.cstapi.variable.Variable;

public interface Visitor {

    default boolean beforeStatement(Statement statement) {
        return true; // go deeper
    }

    default void afterStatement(Statement statement) {
        // do nothing
    }

    default void startSubBlock(int n) {
        // do nothing
    }

    default void endSubBlock(int n) {
        // do nothing
    }

    default boolean beforeExpression(Expression expression) {
        return true; // go deeper
    }

    default void afterExpression(Expression expression) {
        // do nothing
    }

    default boolean beforeVariable(Variable variable) {
        return true;
    }

    default void afterVariable(Variable variable) {
        // do nothing
    }
}
