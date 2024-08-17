package org.e2immu.language.cst.api.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.LocalVariable;

public interface InstanceOf extends Expression {
    Expression expression();

    LocalVariable patternVariable();

    ParameterizedType testType();

    interface Builder extends Element.Builder<Builder> {
        @Fluent
        Builder setExpression(Expression expression);

        @Fluent
        Builder setPatternVariable(LocalVariable patternVariable);

        @Fluent
        Builder setTestType(ParameterizedType testType);

        InstanceOf build();
    }
}
