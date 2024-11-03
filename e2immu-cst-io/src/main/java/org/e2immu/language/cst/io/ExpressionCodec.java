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
import java.util.stream.Stream;

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
        List<Codec.EncodedValue> encode(Expression e);

        Expression decode(List<Codec.EncodedValue> list);
    }

    public Codec.EncodedValue encodeExpression(Expression e) {
        if (e == null) {
            return codec.encodeList(context, List.of());
        }
        ECodec eCodec = map.get(e.name());
        assert eCodec != null;
        List<Codec.EncodedValue> list = eCodec.encode(e);
        Codec.EncodedValue e0 = codec.encodeString(context, e.name());
        return codec.encodeList(context, Stream.concat(Stream.of(e0), list.stream()).toList());
    }

    public Expression decodeExpression(Codec.EncodedValue encodedValue) {
        List<Codec.EncodedValue> list = codec.decodeList(context, encodedValue);
        String name = codec.decodeString(context, list.get(0));
        ECodec eCodec = map.get(name);
        return eCodec.decode(list);
    }

    private Codec.EncodedValue encode(String name, Expression... expressions) {
        List<Codec.EncodedValue> subs = Arrays.stream(expressions).map(this::encodeExpression).toList();
        return new CodecImpl.E(name, subs);
    }

    class VariableExpressionCodec implements ECodec {

        @Override
        public List<Codec.EncodedValue> encode(Expression e) {
            return List.of(codec.encodeVariable(context, ((VariableExpression) e).variable()));
        }

        @Override
        public Expression decode(List<Codec.EncodedValue> list) {
            Variable variable = codec.decodeVariable(context, list.get(1));
            return runtime.newVariableExpression(variable);
        }
    }

    class EnclosedExpressionCodec implements ECodec {

        @Override
        public List<Codec.EncodedValue> encode(Expression enclosedExpression) {
            return List.of(encodeExpression(((EnclosedExpression) enclosedExpression).inner()));
        }

        @Override
        public Expression decode(List<Codec.EncodedValue> list) {
            Expression inner = decodeExpression(list.get(1));
            return runtime.newEnclosedExpressionBuilder().setExpression(inner).build();
        }
    }
}
