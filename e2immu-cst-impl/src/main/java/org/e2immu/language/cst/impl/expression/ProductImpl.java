package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.Product;
import org.e2immu.language.cst.api.runtime.Runtime;

import java.util.List;

public class ProductImpl extends BinaryOperatorImpl implements Product {

    public ProductImpl(Runtime runtime, Expression lhs, Expression rhs) {
        super(List.of(), null, runtime.multiplyOperatorInt(), runtime.precedenceMultiplicative(), lhs, rhs,
                runtime.widestType(lhs.parameterizedType(), rhs.parameterizedType()));
    }

}
