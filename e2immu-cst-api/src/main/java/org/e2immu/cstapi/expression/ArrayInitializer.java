package org.e2immu.cstapi.expression;

import java.util.List;

public interface ArrayInitializer extends Expression {
    List<Expression> expressions();

}
