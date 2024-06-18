package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Product;
import org.e2immu.cstapi.runtime.Runtime;

import java.util.List;

public class ProductImpl extends BinaryOperatorImpl implements Product {

    public ProductImpl(Runtime runtime, Expression lhs, Expression rhs) {
        super(List.of(), null, runtime.multiplyOperatorInt(), runtime.precedenceMultiplicative(), lhs, rhs,
                runtime.widestType(lhs.parameterizedType(), rhs.parameterizedType()));
    }

}
