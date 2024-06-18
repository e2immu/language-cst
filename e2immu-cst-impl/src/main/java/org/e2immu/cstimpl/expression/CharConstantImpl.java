package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.expression.CharConstant;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.Predefined;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.TextImpl;

public class CharConstantImpl extends ConstantExpressionImpl<Character> implements CharConstant {

    private final char constant;
    private final ParameterizedType parameterizedType;

    public CharConstantImpl(Predefined predefined, char constant) {
        this(predefined.charParameterizedType(), constant);
    }

    protected CharConstantImpl(ParameterizedType parameterizedType, char constant) {
        this.parameterizedType = parameterizedType;
        this.constant = constant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharConstantImpl that = (CharConstantImpl) o;
        return constant == that.constant;
    }

    @Override
    public int hashCode() {
        return constant;
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(new TextImpl("'" + escaped(constant) + "'"));
    }

    public static String escaped(char constant) {
        return switch (constant) {
            case '\t' -> "\\t";
            case '\b' -> "\\b";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\f' -> "\\f";
            case '\'' -> "\\'";
            case '\"' -> "\\\"";
            case '\\' -> "\\\\";
            default -> constant >= 32 && constant <= 127 ? Character.toString(constant) :
                    "\\u" + Integer.toString(constant, 16);
        };
    }

    @Override
    public ParameterizedType parameterizedType() {
        return parameterizedType;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_CONSTANT_CHAR;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        return constant - ((CharConstant) expression).constant();
    }

    @Override
    public Character constant() {
        return constant;
    }
}
