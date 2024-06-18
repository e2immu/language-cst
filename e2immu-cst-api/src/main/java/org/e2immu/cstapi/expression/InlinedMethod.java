package org.e2immu.cstapi.expression;

import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.variable.Variable;

import java.util.Set;

public interface InlinedMethod extends Expression {
    MethodInfo methodInfo();

    Expression expression();

    Set<Variable> myParameters();
}
