package org.e2immu.cstapi.expression;

import java.util.List;

public interface Or extends Expression {
    List<Expression> expressions();
}
