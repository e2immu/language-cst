package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.ArrayLength;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.Precedence;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.runtime.Predefined;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.expression.util.ExpressionComparator;
import org.e2immu.language.cst.impl.expression.util.InternalCompareToException;
import org.e2immu.language.cst.impl.expression.util.PrecedenceEnum;
import org.e2immu.language.cst.impl.output.KeywordImpl;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SymbolEnum;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class ArrayLengthImpl extends ExpressionImpl implements ArrayLength {
    private final Expression scope;
    private final ParameterizedType intPt;

    public ArrayLengthImpl(Predefined predefined, Expression scope) {
        this(predefined.intParameterizedType(), scope);
    }

    private ArrayLengthImpl(ParameterizedType intPt, Expression scope) {
        super(1 + scope.complexity());
        this.scope = scope;
        this.intPt = intPt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayLengthImpl that)) return false;
        return scope.equals(that.scope);
    }

    @Override
    public int hashCode() {
        return scope.hashCode();
    }

    @Override
    public Expression scope() {
        return scope;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return intPt;
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.ARRAY_ACCESS;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_ARRAY_LENGTH;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        if (expression instanceof ArrayLength al) {
            return scope.compareTo(al.scope());
        }
        throw new InternalCompareToException();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            scope.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(this)) {
            scope.visit(visitor);
        }
        visitor.afterExpression(this);
    }


    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), scope))
                .add(SymbolEnum.DOT).add(KeywordImpl.LENGTH);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return scope.variables(descendMode);
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return scope.typesReferenced();
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression translatedScope = scope.translate(translationMap);
        if (translatedScope == scope) return this;
        return new ArrayLengthImpl(intPt, translatedScope);
    }
}
