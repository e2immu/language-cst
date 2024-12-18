package org.e2immu.language.cst.impl.expression.eval;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.IntConstant;
import org.e2immu.language.cst.api.expression.Numeric;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.impl.expression.DivideImpl;
import org.e2immu.language.cst.impl.expression.RemainderImpl;

public class EvalRemainder {
    private final Runtime runtime;

    public EvalRemainder(Runtime runtime) {
        this.runtime = runtime;
    }

    public Expression remainder(Expression lhs, Expression rhs) {
        Expression l = runtime.sortAndSimplify(lhs);
        Expression r = runtime.sortAndSimplify(rhs);

        if (l instanceof Numeric ln && ln.doubleValue() == 0) return l;
        if (r instanceof Numeric rn && rn.doubleValue() == 1) return l;
        if (l instanceof IntConstant li && r instanceof IntConstant ri)
            return runtime.newInt(li.constant() % ri.constant());

        // any unknown lingering
        if (l.isEmpty() || r.isEmpty()) throw new UnsupportedOperationException();

        return new RemainderImpl(runtime, l, r);
    }

}
