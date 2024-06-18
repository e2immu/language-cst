package org.e2immu.cstapi.expression;

import org.e2immu.cstapi.runtime.Runtime;

public interface GreaterThanZero extends Expression {
    Expression expression();

    boolean allowEquals();

    XB extract(Runtime runtime);

    interface XB {
        Expression x();
        double b();
        boolean lessThan();
    }
}
