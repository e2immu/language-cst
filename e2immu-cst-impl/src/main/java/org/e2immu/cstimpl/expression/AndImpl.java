package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.And;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.Predefined;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.expression.util.PrecedenceEnum;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.util.ListUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class AndImpl extends ExpressionImpl implements And {

    private final List<Expression> expressions;
    private final ParameterizedType booleanPt;

    public AndImpl(Predefined predefined, List<Expression> expressions) {
        this(predefined.booleanParameterizedType(), expressions);
    }

    private AndImpl(ParameterizedType booleanPt, List<Expression> expressions) {
        super(1 + expressions.stream().mapToInt(Expression::complexity).sum());
        this.expressions = expressions;
        this.booleanPt = booleanPt;
    }

    @Override
    public List<Expression> expressions() {
        return expressions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        And andValue = (And) o;
        return expressions.equals(andValue.expressions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        Precedence precedence = precedence();
        return new OutputBuilderImpl()
                .add(expressions.stream().map(e -> outputInParenthesis(qualification, precedence, e))
                        .collect(OutputBuilderImpl.joining(SymbolEnum.LOGICAL_AND)));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return expressions.stream().flatMap(Expression::typesReferenced);
    }

    @Override
    public ParameterizedType parameterizedType() {
        return booleanPt;
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.LOGICAL_AND;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_AND;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        And andValue = (And) expression;
        return ListUtil.compare(expressions, andValue.expressions());
    }


    @Override
    public Stream<Variable> variables(DescendMode descendIntoFieldReferences) {
        return expressions.stream().flatMap(v -> v.variables(descendIntoFieldReferences));
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
            expressions.forEach(e -> e.visit(visitor));
        }
        visitor.afterExpression(this);
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;
        List<Expression> translatedExpressions = expressions.isEmpty() ? expressions : expressions.stream()
                .map(e -> e.translate(translationMap))
                .collect(translationMap.toList(expressions));
        if (expressions == translatedExpressions) return this;
        return new AndImpl(booleanPt, translatedExpressions);
    }
}
