package org.e2immu.cstapi.variable;

import org.e2immu.cstapi.expression.Expression;

public interface DependentVariable extends Variable{
    Variable arrayVariable();

    Variable indexVariable();

    Expression arrayExpression();

    Expression indexExpression();
}
