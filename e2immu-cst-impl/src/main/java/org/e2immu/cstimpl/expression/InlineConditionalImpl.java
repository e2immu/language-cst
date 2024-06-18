package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.InlineConditional;
import org.e2immu.cstapi.expression.Negation;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.Factory;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.element.ElementImpl;
import org.e2immu.cstimpl.expression.util.InternalCompareToException;
import org.e2immu.cstimpl.expression.util.PrecedenceEnum;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InlineConditionalImpl extends ExpressionImpl implements InlineConditional {

    private final Expression condition;
    private final Expression ifTrue;
    private final Expression ifFalse;
    private final ParameterizedType commonType;


    public InlineConditionalImpl(List<Comment> comments, Source source, Expression condition, Expression ifTrue,
                                 Expression ifFalse, ParameterizedType commonType) {
        super(comments, source, 1 + condition.complexity() + ifTrue.complexity() + ifFalse.complexity());
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        this.commonType = commonType;
    }

    public static final class Builder extends ElementImpl.Builder<InlineConditional.Builder> implements InlineConditional.Builder {
        private Expression condition;
        private Expression ifTrue;
        private Expression ifFalse;

        @Override
        public InlineConditional.Builder setIfTrue(Expression ifTrue) {
            this.ifTrue = ifTrue;
            return this;
        }

        @Override
        public InlineConditional.Builder setIfFalse(Expression ifFalse) {
            this.ifFalse = ifFalse;
            return this;
        }

        @Override
        public InlineConditional.Builder setCondition(Expression condition) {
            this.condition = condition;
            return this;
        }

        @Override
        public InlineConditional build(Factory runtime) {
            return new InlineConditionalImpl(comments, source, condition, ifTrue, ifFalse,
                    runtime.commonType(ifTrue.parameterizedType(), ifFalse.parameterizedType()));
        }
    }

    @Override
    public Expression conditionOfInlineConditional() {
        return condition;
    }

    @Override
    public Expression ifFalse() {
        return ifFalse;
    }

    @Override
    public Expression condition() {
        return condition;
    }

    @Override
    public Expression ifTrue() {
        return ifTrue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InlineConditionalImpl that = (InlineConditionalImpl) o;
        return Objects.equals(condition, that.condition)
               && Objects.equals(ifTrue, that.ifTrue)
               && Objects.equals(ifFalse, that.ifFalse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, ifTrue, ifFalse);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), condition))
                .add(SymbolEnum.QUESTION_MARK)
                .add(outputInParenthesis(qualification, precedence(), ifTrue))
                .add(SymbolEnum.COLON)
                .add(outputInParenthesis(qualification, precedence(), ifFalse));
    }


    @Override
    public ParameterizedType parameterizedType() {
        return commonType;
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.TERNARY;
    }

    @Override
    public int order() {
        return condition.order();
    }

    @Override
    public int internalCompareTo(Expression expression) {
        if (expression instanceof InlineConditional other) {
            int c = condition.compareTo(other.condition());
            if (c == 0) {
                int d = ifTrue.compareTo(other.ifTrue());
                if (d == 0) {
                    return ifFalse.compareTo(other.ifFalse());
                }
                return d;
            }
            return c;
        }
        throw new InternalCompareToException();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            condition.visit(predicate);
            ifTrue.visit(predicate);
            ifFalse.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(this)) {
            condition.visit(visitor);
            ifTrue.visit(visitor);
            ifFalse.visit(visitor);
        }
        visitor.afterExpression(this);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendIntoFieldReferences) {
        return Stream.concat(condition.variables(descendIntoFieldReferences), Stream.concat(
                ifTrue.variables(descendIntoFieldReferences),
                ifFalse.variables(descendIntoFieldReferences)));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.concat(condition.typesReferenced(),
                Stream.concat(ifTrue.typesReferenced(), ifFalse.typesReferenced()));
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression tc = condition.translate(translationMap);
        Expression tt = ifTrue.translate(translationMap);
        Expression tf = ifFalse.translate(translationMap);
        if (tc == condition && tt == ifTrue && tf == ifFalse) return this;
        InlineConditional result = tc instanceof Negation negation
                ? new InlineConditionalImpl(comments(), source(), negation.expression(), tf, tt, commonType)
                : new InlineConditionalImpl(comments(), source(), tc, tt, tf, commonType);
        if (translationMap.translateAgain()) {
            return result.translate(translationMap);
        }
        return result;
    }
}
