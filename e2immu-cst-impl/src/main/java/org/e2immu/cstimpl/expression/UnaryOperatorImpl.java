package org.e2immu.cstimpl.expression;

import org.e2immu.annotation.NotNull;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.expression.UnaryOperator;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class UnaryOperatorImpl extends ExpressionImpl implements UnaryOperator {
    public final Expression expression;
    public final Precedence precedence;
    public final MethodInfo operator;

    public UnaryOperatorImpl(@NotNull MethodInfo operator, @NotNull Expression expression, Precedence precedence) {
        super(1);
        this.expression = Objects.requireNonNull(expression);
        this.precedence = precedence;
        this.operator = Objects.requireNonNull(operator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnaryOperatorImpl that = (UnaryOperatorImpl) o;
        return expression.equals(that.expression) &&
               operator.equals(that.operator);
    }

    public Expression expression() {
        return expression;
    }

    @Override
    public MethodInfo operator() {
        return operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, operator);
    }


    @Override
    public int order() {
        return ExpressionComparator.ORDER_UNARY_OPERATOR; // not yet evaluated
    }

    @Override
    public int internalCompareTo(Expression v) {
        return expression.compareTo(((UnaryOperatorImpl) v).expression);
    }

    @Override
    public ParameterizedType parameterizedType() {
        return expression.parameterizedType();
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        if (operator.isPostfix()) {
            return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence, expression))
                    .add(SymbolEnum.plusPlusSuffix(operator.name()));
        }
        return new OutputBuilderImpl().add(SymbolEnum.plusPlusPrefix(operator.name()))
                .add(outputInParenthesis(qualification, precedence, expression));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return expression.variables(descendMode);
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return expression.typesReferenced();
    }

    @Override
    public Precedence precedence() {
        return precedence;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            expression.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(this)) {
            expression.visit(visitor);
        }
        visitor.afterExpression(this);
    }

    @Override
    public boolean isNumeric() {
        return expression.isNumeric();
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression translatedExpression = expression.translate(translationMap);
        if (translatedExpression == expression) return this;
        return new UnaryOperatorImpl(operator, translatedExpression, precedence);
    }
}
