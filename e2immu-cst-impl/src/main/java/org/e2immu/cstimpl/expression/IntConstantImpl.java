package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.IntConstant;
import org.e2immu.cstapi.expression.Numeric;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.Predefined;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.TextImpl;

public class IntConstantImpl extends ConstantExpressionImpl<Integer> implements Numeric, IntConstant {

    private final int value;
    private final ParameterizedType parameterizedType;

    public IntConstantImpl(Predefined predefined, int value) {
        this(predefined.intParameterizedType(), value);
    }

    protected IntConstantImpl(ParameterizedType parameterizedType, int value) {
        this.parameterizedType = parameterizedType;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntConstantImpl that = (IntConstantImpl) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(new TextImpl(Integer.toString(value)));
    }

    @Override
    public Number number() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return parameterizedType;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_CONSTANT_INT;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        return value - ((IntConstant) expression).constant();
    }

    @Override
    public Integer constant() {
        return value;
    }

    @Override
    public Expression negate() {
        return new IntConstantImpl(parameterizedType, -value);
    }
}
