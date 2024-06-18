package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.expression.Product;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.type.ParameterizedType;

import java.util.List;

public class DivideImpl extends BinaryOperatorImpl implements Product {

    public DivideImpl(List<Comment> comments, Source source, MethodInfo operator, Precedence precedence,
                      Expression lhs, Expression rhs, ParameterizedType parameterizedType) {
        super(comments, source, operator, precedence, lhs, rhs, parameterizedType);
    }

    public DivideImpl(Runtime runtime, Expression lhs, Expression rhs) {
        super(List.of(), null, runtime.divideOperatorInt(), runtime.precedenceMultiplicative(), lhs,
                rhs, runtime.widestType(lhs.parameterizedType(), rhs.parameterizedType()));
    }
}
