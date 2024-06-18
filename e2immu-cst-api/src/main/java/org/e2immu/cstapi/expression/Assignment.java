package org.e2immu.cstapi.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.variable.Variable;

public interface Assignment extends Expression {
    Expression target();

    Expression value();

    Variable variableTarget();

    MethodInfo assignmentOperator();

    Boolean prefixPrimitiveOperator();

    MethodInfo binaryOperator();

    Assignment withValue(Expression ve);

    interface Builder extends Element.Builder<Assignment.Builder> {
        @Fluent
        Builder setTarget(Expression target);

        @Fluent
        Builder setValue(Expression value);

        @Fluent
        Builder setAssignmentOperator(MethodInfo assignmentOperator);

        @Fluent
        Builder setPrefixPrimitiveOperator(Boolean prefixPrimitiveOperator);

        @Fluent
        Builder setBinaryOperator(MethodInfo binaryOperator);

        @Fluent
        Builder setAssignmentOperatorIsPlus(boolean assignmentOperatorIsPlus);

        Assignment build();
    }
}
