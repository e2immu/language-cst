package org.e2immu.language.cst.api.expression;

import java.util.List;

public interface And extends Expression {
    List<Expression> expressions();
}
