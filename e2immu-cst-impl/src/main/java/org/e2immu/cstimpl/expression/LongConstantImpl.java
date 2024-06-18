package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.IntConstant;
import org.e2immu.cstapi.expression.LongConstant;
import org.e2immu.cstapi.expression.Numeric;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.Predefined;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.TextImpl;

public class LongConstantImpl extends ConstantExpressionImpl<Long> implements Numeric, LongConstant {

    private final long value;
    private final ParameterizedType parameterizedType;

    public LongConstantImpl(Predefined predefined, long value) {
        this(predefined.longParameterizedType(), value);
    }

    protected LongConstantImpl(ParameterizedType parameterizedType, long value) {
        this.parameterizedType = parameterizedType;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongConstantImpl that = (LongConstantImpl) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(new TextImpl(Long.toString(value)));
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
        return ExpressionComparator.ORDER_CONSTANT_LONG;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        return (int) Math.signum(value - ((IntConstant) expression).constant());
    }

    @Override
    public Long constant() {
        return value;
    }

    @Override
    public Expression negate() {
        return new LongConstantImpl(parameterizedType, -value);
    }
}
