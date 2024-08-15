package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.expression.BitwiseNegation;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.Precedence;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.impl.util.IntUtil;

public class BitwiseNegationImpl extends UnaryOperatorImpl implements BitwiseNegation {

    public BitwiseNegationImpl(MethodInfo operator, Precedence precedence, Expression expression) {
        super(operator, expression, precedence);
    }

    @Override
    public Double numericValue() {
        Double d = expression.numericValue();
        if (d != null && IntUtil.isMathematicalInteger(d)) {
            long bitwiseNegation = ~((long) (double) d);
            return (double) bitwiseNegation;
        }
        return null;
    }

    @Override
    public int wrapperOrder() {
        return 0;
    }
}
