package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.Product;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.translate.TranslationMap;

import java.util.List;

public class ProductImpl extends BinaryOperatorImpl implements Product {

    private final Runtime runtime;

    public ProductImpl(Runtime runtime, Expression lhs, Expression rhs) {
        super(List.of(), null, runtime.multiplyOperatorInt(), runtime.precedenceMultiplicative(), lhs, rhs,
                runtime.widestType(lhs.parameterizedType(), rhs.parameterizedType()));
        this.runtime = runtime;
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;
        Expression tl = lhs.translate(translationMap);
        Expression tr = rhs.translate(translationMap);
        if (tl == lhs && tr == rhs) return this;
        return new ProductImpl(runtime, tl, tr);
    }

}
