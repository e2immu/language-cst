package org.e2immu.language.cst.impl.expression.eval;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.runtime.Runtime;

public class EvalInequality {
    private final Runtime runtime;

    public EvalInequality(Runtime runtime) {
        this.runtime = runtime;
    }

    public Expression greaterThanZero(Expression expression) {
        throw new UnsupportedOperationException();
    }

    public Expression less(Expression lhs, Expression rhs, boolean allowEquals) {
        throw new UnsupportedOperationException();
    }

    public Expression greater(Expression lhs, Expression rhs, boolean allowEquals) {
        throw new UnsupportedOperationException();
    }
}