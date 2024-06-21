package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.NullConstant;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.impl.expression.util.ExpressionComparator;
import org.e2immu.language.cst.impl.output.KeywordImpl;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;

public class NullConstantImpl extends ConstantExpressionImpl<Object> implements NullConstant {

    private final ParameterizedType parameterizedType;

    public NullConstantImpl(ParameterizedType parameterizedType) {
        super(1);
        assert parameterizedType.isTypeOfNullConstant();
        this.parameterizedType = parameterizedType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullConstant;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Object constant() {
        return null;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return parameterizedType;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_CONSTANT_NULL;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        return 0; // there's only one
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(KeywordImpl.NULL);
    }
}
