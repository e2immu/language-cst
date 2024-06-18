package org.e2immu.language.cst.api.variable;

import org.e2immu.language.cst.api.expression.Expression;

public interface DependentVariable extends Variable{
    Variable arrayVariable();

    Variable indexVariable();

    Expression arrayExpression();

    Expression indexExpression();
}
