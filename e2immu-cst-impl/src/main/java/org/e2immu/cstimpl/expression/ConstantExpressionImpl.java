package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.ConstantExpression;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.expression.util.PrecedenceEnum;

import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class ConstantExpressionImpl<T> extends ExpressionImpl implements ConstantExpression<T> {

    protected ConstantExpressionImpl() {
        super(1);
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.TOP;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        predicate.test(this);
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.beforeExpression(this);
        visitor.afterExpression(this);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.empty();
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.empty();
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        return translationMap.translateExpression(this);
    }
}
