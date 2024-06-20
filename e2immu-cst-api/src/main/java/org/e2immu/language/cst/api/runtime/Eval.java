package org.e2immu.language.cst.api.runtime;

import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.api.expression.*;

import java.util.List;
import java.util.stream.Stream;

public interface Eval {

    Expression inlineConditional(Expression condition, Expression ifTrue, Expression ifFalse,
                                 Variable myself, boolean modifying);

    Expression product(Expression lhs, Expression rhs);

    Expression sum(Expression lhs, Expression rhs);

    Expression negate(Expression expression);

    Expression remainder(Expression lhs, Expression rhs);

    Expression equals(Expression lhs, Expression rhs);

    Expression greater(Expression lhs, Expression rhs, boolean allowEquals);

    Expression greaterThanZero(Expression expression);

    Expression or(List<Expression> expressions);

    Expression or(Expression... expressions);

    Expression and(Expression... expressions);

    Expression and(List<Expression> expressions);

    Expression divide(Expression lhs, Expression rhs);

    Expression binaryOperator(BinaryOperator binaryOperator);

    default Expression sortAndSimplify(Expression expression) {
        if (expression instanceof Sum sum) {
            return sum(sum.lhs(), sum.rhs());
        }
        if (expression instanceof Product product) {
            return product(product.lhs(), product.rhs());
        }
        if (expression instanceof InlineConditional i) {
            return inlineConditional(i.condition(), i.ifTrue(), i.ifFalse(), null, true);
        }
        if (expression instanceof Or or) {
            return or(or.expressions());
        }
        if (expression instanceof And and) {
            return and(and.expressions());
        }
        if (expression instanceof GreaterThanZero gt0) {
            return greaterThanZero(gt0.expression());
        }
        if (expression instanceof Equals equals) {
            return equals(equals.lhs(), equals.rhs());
        }
        if (expression instanceof Negation negation) {
            return negate(negation.expression());
        }
        if (expression instanceof Divide divide) {
            return divide(divide.lhs(), divide.rhs());
        }
        if(expression instanceof BinaryOperator binaryOperator) {
            return binaryOperator(binaryOperator);
        }
        return expression;
    }

    boolean isNotNull0(Expression expression);

    int limitOnComplexity();

    Stream<Expression> expandTerms(Expression expression, boolean negate);
    
    Stream<Expression> expandFactors(Expression expression);

    Expression less(Expression lhs, Expression rhs, boolean allowEquals);

    Expression wrapInProduct(Expression[] translated, int length);

    Expression wrapInSum(Expression[] translated, int length);
}
