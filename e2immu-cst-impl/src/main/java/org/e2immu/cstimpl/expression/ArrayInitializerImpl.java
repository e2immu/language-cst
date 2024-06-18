package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.ArrayInitializer;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.expression.util.InternalCompareToException;
import org.e2immu.cstimpl.expression.util.PrecedenceEnum;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.type.ParameterizedTypeImpl;
import org.e2immu.cstimpl.util.ListUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ArrayInitializerImpl extends ExpressionImpl implements ArrayInitializer {

    private final ParameterizedType commonType;
    private final List<Expression> expressions;

    public ArrayInitializerImpl(List<Expression> expressions, ParameterizedType commonType) {
        super(expressions.stream().mapToInt(Expression::complexity).sum() + 1);
        this.commonType = commonType;
        this.expressions = expressions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayInitializerImpl that = (ArrayInitializerImpl) o;
        return Objects.equals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(expressions);
    }

    @Override
    public List<Expression> expressions() {
        return expressions;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return new ParameterizedTypeImpl(commonType.typeInfo(), 1);
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.BOTTOM;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_ARRAY;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        if (expression instanceof ArrayInitializer ai) {
            return ListUtil.compare(expressions, ai.expressions());
        }
        throw new InternalCompareToException();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            expressions.forEach(v -> v.visit(predicate));
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(this)) {
            expressions.forEach(v -> v.visit(visitor));
        }
        visitor.afterExpression(this);
    }

    @Override
    public boolean isConstant() {
        return expressions.stream().allMatch(Expression::isConstant);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl()
                .add(SymbolEnum.LEFT_BRACE)
                .add(expressions.stream().map(expression -> expression.print(qualification))
                        .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)))
                .add(SymbolEnum.RIGHT_BRACE);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return expressions.stream().flatMap(e -> e.variables(descendMode));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return expressions.stream().flatMap(Expression::typesReferenced);
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;
        List<Expression> translatedExpressions = expressions.stream()
                .map(e -> e.translate(translationMap))
                .collect(translationMap.toList(expressions));
        ParameterizedType translatedType = translationMap.translateType(commonType);
        if (translatedType == commonType && translatedExpressions == expressions) return this;
        return new ArrayInitializerImpl(translatedExpressions, translatedType);
    }
}
