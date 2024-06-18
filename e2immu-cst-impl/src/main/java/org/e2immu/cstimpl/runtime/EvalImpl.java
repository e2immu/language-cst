package org.e2immu.cstimpl.runtime;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.runtime.Eval;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.expression.ExpressionImpl;
import org.e2immu.cstimpl.expression.eval.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class EvalImpl implements Eval {
    private final EvalProduct evalProduct;
    private final EvalSum evalSum;
    private final EvalNegation evalNegation;
    private final EvalEquals evalEquals;
    private final EvalAnd evalAnd;
    private final EvalOr evalOr;
    private final EvalDivide evalDivide;
    private final EvalInlineConditional evalInlineConditional;
    private final EvalInequality evalInequality;

    public EvalImpl(Runtime runtime) {
        evalProduct = new EvalProduct(runtime);
        evalSum = new EvalSum(runtime);
        evalNegation = new EvalNegation(runtime);
        evalEquals = new EvalEquals(runtime);
        evalAnd = new EvalAnd(runtime);
        evalOr = new EvalOr(runtime);
        evalDivide = new EvalDivide(runtime);
        evalInlineConditional = new EvalInlineConditional(runtime);
        evalInequality = new EvalInequality(runtime);
    }

    @Override
    public Expression inlineConditional(Expression condition, Expression ifTrue, Expression ifFalse, Variable myself, boolean modifying) {
        return evalInlineConditional.eval(condition, ifTrue, ifFalse, myself, modifying);
    }

    @Override
    public Expression divide(Expression lhs, Expression rhs) {
        return evalDivide.divide(lhs, rhs);
    }

    @Override
    public Expression product(Expression lhs, Expression rhs) {
        return evalProduct.eval(lhs, rhs);
    }

    @Override
    public Expression sum(Expression lhs, Expression rhs) {
        return evalSum.eval(lhs, rhs, true);
    }

    @Override
    public Expression negate(Expression expression) {
        return evalNegation.eval(expression);
    }

    @Override
    public Expression remainder(Expression lhs, Expression rhs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression equals(Expression lhs, Expression rhs) {
        return evalEquals.eval(lhs, rhs);
    }

    @Override
    public Expression greater(Expression lhs, Expression rhs, boolean allowEquals) {
        return evalInequality.greater(lhs, rhs, allowEquals);
    }

    @Override
    public Expression less(Expression lhs, Expression rhs, boolean allowEquals) {
        return evalInequality.less(lhs, rhs, allowEquals);
    }

    @Override
    public Expression wrapInProduct(Expression[] translated, int length) {
        return evalProduct.wrapInProduct(translated, length);
    }

    @Override
    public Expression wrapInSum(Expression[] translated, int length) {
        return evalSum.wrapInSum(translated, length);
    }

    @Override
    public Expression greaterThanZero(Expression expression) {
        return evalInequality.greaterThanZero(expression);
    }

    @Override
    public Expression or(List<Expression> expressions) {
        return evalOr.eval(expressions);
    }

    @Override
    public Expression or(Expression... expressions) {
        return evalOr.eval(Arrays.stream(expressions).toList());
    }

    @Override
    public Expression and(Expression... expressions) {
        return evalAnd.eval(Arrays.stream(expressions).toList());
    }

    @Override
    public Expression and(List<Expression> expressions) {
        return evalAnd.eval(expressions);
    }

    @Override
    public boolean isNotNull0(Expression expression) {
        return true;
    }

    @Override
    public int limitOnComplexity() {
        return ExpressionImpl.SOFT_LIMIT_ON_COMPLEXITY;
    }

    @Override
    public Stream<Expression> expandTerms(Expression expression, boolean negate) {
        return evalSum.expandTerms(expression, negate);
    }

    @Override
    public Stream<Expression> expandFactors(Expression expression) {
        return evalProduct.expandFactors(expression);
    }
}
