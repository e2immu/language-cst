package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.expression.ByteConstant;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.IntConstant;
import org.e2immu.language.cst.api.expression.Numeric;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.runtime.Predefined;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.impl.expression.util.ExpressionComparator;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.TextImpl;

public class ByteConstantImpl extends ConstantExpressionImpl<Byte> implements Numeric, ByteConstant {

    private final byte value;
    private final ParameterizedType parameterizedType;

    public ByteConstantImpl(Predefined predefined, byte value) {
        this(predefined.byteParameterizedType(), value);
    }

    protected ByteConstantImpl(ParameterizedType parameterizedType, byte value) {
        super(value < -1 || value > 1 ? 2 : 1);
        this.parameterizedType = parameterizedType;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteConstantImpl that = (ByteConstantImpl) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(new TextImpl(Byte.toString(value)));
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
        return ExpressionComparator.ORDER_CONSTANT_BYTE;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        return value - ((ByteConstant) expression).constant();
    }

    @Override
    public Byte constant() {
        return value;
    }

    @Override
    public Expression negate() {
        return new ByteConstantImpl(parameterizedType, (byte) -value);
    }
}
