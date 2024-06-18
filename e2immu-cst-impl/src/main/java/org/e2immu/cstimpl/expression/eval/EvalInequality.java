package org.e2immu.cstimpl.expression.eval;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.runtime.Runtime;

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
