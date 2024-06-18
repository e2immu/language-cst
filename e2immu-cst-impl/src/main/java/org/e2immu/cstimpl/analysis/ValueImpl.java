package org.e2immu.cstimpl.analysis;

import org.e2immu.cstapi.analysis.Codec;
import org.e2immu.cstapi.analysis.Value;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.info.FieldInfo;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.ParameterInfo;
import org.e2immu.cstapi.util.ParSeq;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class ValueImpl implements Value {

    private static final Map<Class<? extends Value>, BiFunction<Codec, Codec.EncodedValue, Value>> decoderMap = new HashMap<>();

    public static BiFunction<Codec, Codec.EncodedValue, Value> decoder(Class<? extends Value> clazz) {
        return decoderMap.get(clazz);
    }

    public static class BoolImpl implements Value.Bool {
        public static final Bool FALSE = new BoolImpl(false);
        public static final Bool TRUE = new BoolImpl(true);
        private final boolean value;

        private BoolImpl(boolean value) {
            this.value = value;
        }

        public static Value from(boolean b) {
            return b ? TRUE : FALSE;
        }

        @Override
        public boolean isFalse() {
            return !value;
        }


        @Override
        public boolean isTrue() {
            return value;
        }

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeBoolean(value);
        }
    }

    static {
        decoderMap.put(BoolImpl.class, (codec, encodedValue) -> new BoolImpl(codec.decodeBoolean(encodedValue)));
    }

    public record ParameterParSeqImpl(ParSeq<ParameterInfo> parSeq) implements Value.ParameterParSeq {
        public static ParameterParSeqImpl EMPTY = new ValueImpl.ParameterParSeqImpl(new ParSeq<>() {
            @Override
            public boolean containsParallels() {
                return false;
            }

            @Override
            public <X> List<X> sortParallels(List<X> items, Comparator<X> comparator) {
                return items;
            }
        });

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            throw new UnsupportedOperationException();
        }
    }

    public record CommutableDataImpl(String seq, String par, String multi) implements CommutableData {
        public static final CommutableData BLANK = new ValueImpl.CommutableDataImpl("", "", "");

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeList(List.of(codec.encodeString(seq), codec.encodeString(par),
                    codec.encodeString(multi)));
        }
    }

    static {
        decoderMap.put(CommutableDataImpl.class, (codec, encodedValue) -> {
            List<Codec.EncodedValue> list = codec.decodeList(encodedValue);
            return new CommutableDataImpl(codec.decodeString(list.get(0)), codec.decodeString(list.get(1)),
                    codec.decodeString(list.get(2)));
        });
    }

    public record ImmutableImpl(int value) implements Immutable {
        public static final Immutable MUTABLE = new ImmutableImpl(0);

        @Override
        public boolean isAtLeastImmutableHC() {
            return value > 10; // FIXME
        }

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeInt(value);
        }
    }

    static {
        decoderMap.put(ImmutableImpl.class, (codec, encodedValue) -> new ImmutableImpl(codec.decodeInt(encodedValue)));
    }

    public record FieldValueImpl(FieldInfo field) implements FieldValue {
        public static final FieldValue EMPTY = new FieldValueImpl(null);

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeInfo(field);
        }
    }

    static {
        decoderMap.put(FieldValueImpl.class, (codec, encodedValue)
                -> new FieldValueImpl(codec.decodeFieldInfo(encodedValue)));
    }

    public record FieldBooleanMapImpl(Map<FieldInfo, Boolean> map) implements FieldBooleanMap {
        public static final FieldBooleanMap EMPTY = new FieldBooleanMapImpl(Map.of());

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            Map<Codec.EncodedValue, Codec.EncodedValue> encodedMap = map.entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(e -> codec.encodeInfo(e.getKey()),
                            e -> codec.encodeBoolean(e.getValue())));
            return codec.encodeMap(encodedMap);
        }
    }

    static {
        decoderMap.put(FieldBooleanMapImpl.class, (codec, encodedValue) -> {
            Map<FieldInfo, Boolean> decodedMap = codec.decodeMap(encodedValue)
                    .entrySet().stream().collect(Collectors.toUnmodifiableMap(
                            e -> codec.decodeFieldInfo(e.getKey()),
                            e -> codec.decodeBoolean(e.getValue())));
            return new FieldBooleanMapImpl(decodedMap);
        });
    }

    public record AssignedToFieldImpl(Set<FieldInfo> fields) implements AssignedToField {
        public static final AssignedToField EMPTY = new AssignedToFieldImpl(Set.of());

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            Set<Codec.EncodedValue> set = fields.stream().map(codec::encodeInfo).collect(Collectors.toUnmodifiableSet());
            return codec.encodeSet(set);
        }
    }

    static {
        decoderMap.put(AssignedToFieldImpl.class, (codec, encodedValue) -> {
            Set<FieldInfo> decodedSet = codec.decodeSet(encodedValue).stream()
                    .map(codec::decodeFieldInfo).collect(Collectors.toUnmodifiableSet());
            return new AssignedToFieldImpl(decodedSet);
        });
    }

    public record PostConditionsImpl(Map<String, Expression> byIndex) implements PostConditions {
        public static final PostConditions EMPTY = new PostConditionsImpl(Map.of());

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            Map<Codec.EncodedValue, Codec.EncodedValue> encodedMap = byIndex.entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(e -> codec.encodeString(e.getKey()),
                            e -> codec.encodeExpression(e.getValue())));
            return codec.encodeMap(encodedMap);
        }
    }

    static {
        decoderMap.put(PostConditionsImpl.class, (codec, encodedValue) -> {
            Map<String, Expression> decodeMap = codec.decodeMap(encodedValue).entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(
                            e -> codec.decodeString(e.getKey()),
                            e -> codec.decodeExpression(e.getValue())));
            return new PostConditionsImpl(decodeMap);
        });
    }

    public record PreconditionImpl(Expression expression) implements Precondition {
        public static final Precondition EMPTY = new PreconditionImpl(null);

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeExpression(expression);
        }
    }

    static {
        decoderMap.put(PreconditionImpl.class, (codec, encodedValue) ->
                new PreconditionImpl(codec.decodeExpression(encodedValue)));
    }

    public record IndicesOfEscapesImpl(Set<String> indices) implements IndicesOfEscapes {
        public static final IndicesOfEscapes EMPTY = new IndicesOfEscapesImpl(Set.of());

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            Set<Codec.EncodedValue> set = indices.stream().map(codec::encodeString).collect(Collectors.toUnmodifiableSet());
            return codec.encodeSet(set);
        }
    }

    static {
        decoderMap.put(IndicesOfEscapesImpl.class, (codec, encodedValue) -> {
            Set<String> indices = codec.decodeSet(encodedValue).stream()
                    .map(codec::decodeString).collect(Collectors.toUnmodifiableSet());
            return new IndicesOfEscapesImpl(indices);
        });
    }

    public record GetSetEquivalentImpl(Set<ParameterInfo> parameters,
                                       MethodInfo methodWithoutParameters) implements GetSetEquivalent {
        public static final GetSetEquivalent EMPTY = new GetSetEquivalentImpl(Set.of(), null);

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            Set<Codec.EncodedValue> set = parameters.stream().map(codec::encodeVariable)
                    .collect(Collectors.toUnmodifiableSet());
            return codec.encodeList(List.of(codec.encodeSet(set), codec.encodeInfo(methodWithoutParameters)));
        }
    }

    static  {
        decoderMap.put(GetSetEquivalentImpl.class, (codec, encodedValue) -> {
            List<Codec.EncodedValue> list = codec.decodeList(encodedValue);
            Set<ParameterInfo> set = codec.decodeSet(list.get(0)).stream()
                    .map(codec::decodeParameterInfo).collect(Collectors.toUnmodifiableSet());
           return new GetSetEquivalentImpl(set, codec.decodeMethodInfo(list.get(1)));
        });
    }
}
