package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Negation;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.translate.TranslationMap;

public class NegationImpl extends UnaryOperatorImpl implements Negation {

    public NegationImpl(MethodInfo operator, Precedence precedence, Expression expression) {
        super(operator, expression, precedence);
    }

    @Override
    public Double numericValue() {
        Double d = expression.numericValue();
        return d == null ? null : -d;
    }

    @Override
    public boolean isNegatedOrNumericNegative() {
        return true;
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression translatedExpression = expression.translate(translationMap);
        if (translatedExpression == expression) return this;
        if (translatedExpression instanceof Negation negation) {
            return negation.expression(); // double negation gets cancelled
        }
        return new NegationImpl(operator, precedence, translatedExpression);
    }
}
