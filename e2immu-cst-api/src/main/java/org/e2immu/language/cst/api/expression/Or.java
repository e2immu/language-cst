package org.e2immu.language.cst.api.expression;

import java.util.List;

public interface Or extends Expression {
    List<Expression> expressions();
}
