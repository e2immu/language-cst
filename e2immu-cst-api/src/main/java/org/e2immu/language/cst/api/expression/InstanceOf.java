package org.e2immu.language.cst.api.expression;

import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.LocalVariable;

public interface InstanceOf extends Expression {
    Expression expression();

    LocalVariable patternVariable();

    ParameterizedType parameterizedType();
}
