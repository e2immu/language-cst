package org.e2immu.language.cst.api.expression;

import java.util.List;

public interface ArrayInitializer extends Expression {
    List<Expression> expressions();

    String NAME = "arrayInitializer";

    @Override
    default String name() {
        return NAME;
    }
}
