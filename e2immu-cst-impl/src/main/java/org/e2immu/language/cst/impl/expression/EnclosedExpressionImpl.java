package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.EnclosedExpression;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.Precedence;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.language.cst.impl.expression.util.PrecedenceEnum;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SymbolEnum;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EnclosedExpressionImpl extends ExpressionImpl implements EnclosedExpression {
    private final Expression inner;

    public EnclosedExpressionImpl(List<Comment> comments, Source source, Expression inner) {
        super(comments, source, 1 + inner.complexity());
        this.inner = inner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnclosedExpressionImpl that = (EnclosedExpressionImpl) o;
        return Objects.equals(inner, that.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner);
    }

    @Override
    public Expression inner() {
        return inner;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return inner.parameterizedType();
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.TOP;
    }

    @Override
    public int order() {
        return inner.order();
    }

    @Override
    public int internalCompareTo(Expression expression) {
        return inner.compareTo(((EnclosedExpression) expression).inner());
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(inner)) {
            inner.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(inner)) {
            inner.visit(visitor);
        }
        visitor.afterExpression(inner);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(SymbolEnum.LEFT_PARENTHESIS)
                .add(inner.print(qualification)).add(SymbolEnum.RIGHT_PARENTHESIS);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return inner.variables(descendMode);
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return inner.typesReferenced();
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression translatedInner = inner.translate(translationMap);
        if (translatedInner == inner) return this;
        return new EnclosedExpressionImpl(comments(), source(), translatedInner);
    }

    public static class Builder extends ElementImpl.Builder<EnclosedExpression.Builder> implements EnclosedExpression.Builder {
        private Expression expression;

        @Override
        public EnclosedExpression.Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public EnclosedExpression build() {
            return new EnclosedExpressionImpl(comments, source, expression);
        }
    }
}
