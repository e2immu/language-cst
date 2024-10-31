package org.e2immu.language.cst.io;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.expression.EnclosedExpression;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.VariableExpression;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionCodec {
    private final Codec codec;
    private final Codec.Context context;
    private final Runtime runtime;
    private final Map<String, ECodec> map = new HashMap<>();

    public ExpressionCodec(Runtime runtime, Codec codec, Codec.Context context) {
        this.runtime = runtime;
        this.codec = codec;
        this.context = context;
        map.put(VariableExpression.NAME, new VariableExpressionCodec());
        map.put(EnclosedExpression.NAME, new EnclosedExpressionCodec());
    }

    private interface ECodec {
        Codec.EncodedValue encode(Expression e);

        Expression decode(Codec.EncodedValue encodedValue);
    }

    public Codec.EncodedValue encodeExpression(Expression e) {
        ECodec eCodec = map.get(e.name());
        assert eCodec != null;
        return eCodec.encode(e);
    }

    public Expression decodeExpression(Codec.EncodedValue encodedValue) {
        if (encodedValue instanceof CodecImpl.E e) {
            ECodec eCodec = map.get(e.s());
            return eCodec.decode(encodedValue);
        } else throw new UnsupportedOperationException();
    }

    private Codec.EncodedValue encode(String name, Expression... expressions) {
        List<Codec.EncodedValue> subs = Arrays.stream(expressions).map(this::encodeExpression).toList();
        return new CodecImpl.E(name, subs);
    }

    class VariableExpressionCodec implements ECodec {

        @Override
        public Codec.EncodedValue encode(Expression e) {
            return new CodecImpl.E(VariableExpression.NAME,
                    List.of(codec.encodeVariable(context, ((VariableExpression) e).variable())));
        }

        @Override
        public Expression decode(Codec.EncodedValue encodedValue) {
            if (encodedValue instanceof CodecImpl.E e) {
                assert VariableExpression.NAME.equals(e.s());
                Variable variable = codec.decodeVariable(context, e.subs().get(0));
                return runtime.newVariableExpression(variable);
            } else throw new UnsupportedOperationException();
        }
    }

    class EnclosedExpressionCodec implements ECodec {

        @Override
        public Codec.EncodedValue encode(Expression enclosedExpression) {
            return ExpressionCodec.this.encode(enclosedExpression.name(), ((EnclosedExpression) enclosedExpression).inner());
        }

        @Override
        public Expression decode(Codec.EncodedValue encodedValue) {
            if (encodedValue instanceof CodecImpl.E e) {
                assert EnclosedExpression.NAME.equals(e.s());
                Expression inner = decodeExpression(e.subs().get(0));
                return runtime.newEnclosedExpressionBuilder().setExpression(inner).build();
            } else throw new UnsupportedOperationException();
        }
    }
}
