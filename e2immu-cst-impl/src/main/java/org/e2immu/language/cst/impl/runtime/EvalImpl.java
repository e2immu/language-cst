package org.e2immu.language.cst.impl.runtime;

import org.e2immu.language.cst.api.expression.BinaryOperator;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.InstanceOf;
import org.e2immu.language.cst.api.expression.UnaryOperator;
import org.e2immu.language.cst.api.runtime.Eval;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.expression.ExpressionImpl;
import org.e2immu.language.cst.impl.expression.eval.*;

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
    private final EvalBinaryOperator evalBinaryOperator;
    private final EvalInstanceOf evalInstanceOf;
    private final EvalUnaryOperator evalUnaryOperator;

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
        evalBinaryOperator = new EvalBinaryOperator(runtime);
        evalInstanceOf = new EvalInstanceOf(runtime);
        evalUnaryOperator = new EvalUnaryOperator(runtime);
    }

    @Override
    public Expression instanceOf(InstanceOf instanceOf) {
        return evalInstanceOf.eval(instanceOf);
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
    public Expression binaryOperator(BinaryOperator binaryOperator) {
        return evalBinaryOperator.eval(binaryOperator);
    }

    @Override
    public Expression unaryOperator(UnaryOperator unaryOperator) {
        return evalUnaryOperator.eval(unaryOperator);
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
    public Expression greaterThanZero(Expression expression, boolean allowEquals) {
        return evalInequality.greaterThanZero(expression, allowEquals);
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
