package org.e2immu.language.cst.api.expression;

import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.Set;

public interface InlinedMethod extends Expression {
    MethodInfo methodInfo();

    Expression expression();

    Set<Variable> myParameters();
}
