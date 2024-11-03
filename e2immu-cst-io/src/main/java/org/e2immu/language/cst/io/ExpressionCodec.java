package org.e2immu.language.cst.io;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.type.Diamond;
import org.e2immu.language.cst.api.type.ParameterizedType;
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
        map.put(ConstructorCall.NAME, new ConstructorCallCodec());
        map.put(NullConstant.NAME, new NullConstantCodec());
        map.put(Cast.NAME, new CastCodec());
        map.put(BooleanConstant.NAME, new BooleanConstantCodec());
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
        assert eCodec != null : "No codec yet for " + e.name();
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

    class ConstructorCallCodec implements ECodec {

        @Override
        public List<Codec.EncodedValue> encode(Expression e) {
            ConstructorCall cc = (ConstructorCall) e;
            return List.of(codec.encodeInfoOutOfContext(context, cc.constructor()),
                    encodeDiamond(cc.diamond()),
                    codec.encodeType(context, cc.parameterizedType()),
                    codec.encodeExpression(context, cc.object()),
                    codec.encodeList(context, cc.parameterExpressions().stream()
                            .map(ExpressionCodec.this::encodeExpression).toList()),
                    codec.encodeExpression(context, cc.arrayInitializer()));
        }

        @Override
        public Expression decode(List<Codec.EncodedValue> list) {
            MethodInfo constructor = (MethodInfo) codec.decodeInfoOutOfContext(context, list.get(1));
            Diamond diamond = decodeDiamond(list.get(2));
            ParameterizedType concreteReturnType = codec.decodeType(context, list.get(3));
            Expression object = codec.decodeExpression(context, list.get(4));
            List<Expression> arguments = codec.decodeList(context, list.get(5))
                    .stream().map(e -> codec.decodeExpression(context, e)).toList();
            ArrayInitializer arrayInitializer = (ArrayInitializer) codec.decodeExpression(context, list.get(6));
            return runtime.newConstructorCallBuilder()
                    .setConstructor(constructor)
                    .setDiamond(diamond)
                    .setConcreteReturnType(concreteReturnType)
                    .setObject(object)
                    .setParameterExpressions(arguments)
                    .setArrayInitializer(arrayInitializer)
                    .build();
        }

        private Codec.EncodedValue encodeDiamond(Diamond diamond) {
            String s;
            if (diamond.isNo()) s = "N";
            else if (diamond.isYes()) s = "Y";
            else if (diamond.isShowAll()) s = "A";
            else throw new UnsupportedOperationException();
            return codec.encodeString(context, s);
        }

        private Diamond decodeDiamond(Codec.EncodedValue encodedValue) {
            String s = codec.decodeString(context, encodedValue);
            return switch (s) {
                case "Y" -> runtime.diamondYes();
                case "N" -> runtime.diamondNo();
                case "A" -> runtime.diamondShowAll();
                default -> throw new UnsupportedOperationException();
            };
        }
    }

    class NullConstantCodec implements ECodec {

        @Override
        public List<Codec.EncodedValue> encode(Expression e) {
            return List.of();
        }

        @Override
        public Expression decode(List<Codec.EncodedValue> list) {
            return runtime.nullConstant();
        }
    }

    class CastCodec implements ECodec {

        @Override
        public List<Codec.EncodedValue> encode(Expression e) {
            Cast cast = (Cast) e;
            return List.of(codec.encodeType(context, cast.parameterizedType()),
                    codec.encodeExpression(context, cast.expression()));
        }

        @Override
        public Expression decode(List<Codec.EncodedValue> list) {
            ParameterizedType type = codec.decodeType(context, list.get(1));
            Expression expression = codec.decodeExpression(context, list.get(2));
            return runtime.newCastBuilder().setParameterizedType(type).setExpression(expression).build();
        }
    }

    class BooleanConstantCodec implements ECodec {

        @Override
        public List<Codec.EncodedValue> encode(Expression e) {
            return List.of(codec.encodeBoolean(context, e.isBoolValueTrue()));
        }

        @Override
        public Expression decode(List<Codec.EncodedValue> list) {
            return runtime.newBoolean(codec.decodeBoolean(context, list.get(1)));
        }
    }
}
