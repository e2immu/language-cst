package org.e2immu.cstapi.expression;

import java.util.List;

public interface And extends Expression {
    List<Expression> expressions();
}
