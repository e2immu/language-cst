package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.expression.Equals;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;

import java.util.List;

public class EqualsImpl extends BinaryOperatorImpl implements Equals {

    public EqualsImpl(List<Comment> comments, Source source, MethodInfo operator, Precedence precedence,
                      Expression lhs, Expression rhs, ParameterizedType parameterizedType) {
        super(comments, source, operator, precedence, lhs, rhs, parameterizedType);
    }

    public EqualsImpl(Runtime runtime, Expression lhs, Expression rhs) {
        super(List.of(), null, lhs.isNumeric() ? runtime.equalsOperatorInt() : runtime.equalsOperatorObject(),
                runtime.precedenceEquality(), lhs, rhs,
                runtime.booleanParameterizedType());
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression tl = lhs.translate(translationMap);
        Expression tr = rhs.translate(translationMap);
        if (tl == lhs && tr == rhs) return this;
        return new EqualsImpl(comments(), source(), operator, precedence, tl, tr, parameterizedType);
    }
}
