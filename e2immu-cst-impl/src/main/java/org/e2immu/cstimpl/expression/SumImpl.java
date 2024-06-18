package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Negation;
import org.e2immu.cstapi.expression.Numeric;
import org.e2immu.cstapi.expression.Sum;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;

import java.util.List;


public class SumImpl extends BinaryOperatorImpl implements Sum {

    public SumImpl(Runtime runtime, Expression lhs, Expression rhs) {
        super(List.of(), null, runtime.plusOperatorInt(), runtime.precedenceAdditive(), lhs, rhs,
                runtime.widestType(lhs.parameterizedType(), rhs.parameterizedType()));
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_SUM;
    }

    /*
        Generate the expression lhs+rhs == 0.
        Handle lhs-rhs==0  === lhs==rhs, etc.
        This is part of the system that computes equality.
         */
    @Override
    public Expression isZero(Runtime runtime) {
        if (lhs instanceof Negation negation && !(rhs instanceof Negation)) {
            return runtime.equals(negation.expression(), rhs);
        }
        if (rhs instanceof Negation negation && !(lhs instanceof Negation)) {
            return runtime.equals(lhs, negation.expression());
        }
        if (lhs instanceof Negation nLhs) {
            return runtime.equals(nLhs, rhs);
        }
        return runtime.equals(lhs, runtime.negate(rhs));
    }

    /*
    extra code to avoid writing a+-b ~ a-b
     */
    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), lhs));
        boolean ignoreOperator = rhs instanceof Negation || rhs instanceof Sum sum2 && (sum2.lhs() instanceof Negation);
        if (!ignoreOperator) {
            outputBuilder.add(SymbolEnum.binaryOperator(operator.name()));
        }
        return outputBuilder.add(outputInParenthesis(qualification, precedence(), rhs));
    }

    // recursive method
    public Double numericPartOfLhs() {
        Numeric n;
        if ((n = lhs.asInstanceOf(Numeric.class)) != null) return n.doubleValue();
        Sum s;
        if ((s = lhs.asInstanceOf(Sum.class)) != null) return s.numericPartOfLhs();
        return null;
    }

    // can only be called when there is a numeric part somewhere!
    public Expression nonNumericPartOfLhs(Runtime runtime) {
        if (lhs.isInstanceOf(Numeric.class)) return rhs;
        Sum s;
        if ((s = lhs.asInstanceOf(Sum.class)) != null) {
            // the numeric part is somewhere inside lhs
            Expression nonNumeric = s.nonNumericPartOfLhs(runtime);
            return runtime.sum(nonNumeric, rhs);
        }
        throw new UnsupportedOperationException();
    }
}
