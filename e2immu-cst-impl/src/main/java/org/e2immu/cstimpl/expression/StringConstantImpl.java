package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.StringConstant;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.Predefined;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.TextImpl;
import org.e2immu.cstimpl.util.StringUtil;

import java.util.Objects;

public class StringConstantImpl extends ConstantExpressionImpl<String> implements StringConstant {
    private final ParameterizedType stringPt;
    private final String constant;

    public StringConstantImpl(Predefined predefined, String constant) {
        this(predefined.stringParameterizedType(), constant);
    }

    protected StringConstantImpl(ParameterizedType stringPt, String constant) {
        this.stringPt = Objects.requireNonNull(stringPt);
        this.constant = Objects.requireNonNull(constant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringConstantImpl that)) return false;
        return constant.equals(that.constant);
    }

    @Override
    public int hashCode() {
        return constant.hashCode();
    }

    @Override
    public String constant() {
        return constant;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return stringPt;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_CONSTANT_BOOLEAN;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        StringConstant sc = (StringConstant) expression;
        return constant.compareTo(sc.constant());
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(new TextImpl(StringUtil.quote(constant)));
    }
}
