package org.e2immu.language.cst.impl.analysis;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.FieldInfo;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.info.ParameterInfo;
import org.e2immu.language.cst.api.util.ParSeq;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class ValueImpl implements Value {

    private static final Map<Class<? extends Value>, BiFunction<Codec, Codec.EncodedValue, Value>> decoderMap = new HashMap<>();

    public static BiFunction<Codec, Codec.EncodedValue, Value> decoder(Class<? extends Value> clazz) {
        return decoderMap.get(clazz);
    }

    public static class BoolImpl implements Value.Bool {
        public static final Bool NO_VALUE = new BoolImpl(-1);
        public static final Bool FALSE = new BoolImpl(0);
        public static final Bool TRUE = new BoolImpl(1);
        private final int value;

        private BoolImpl(int value) {
            this.value = value;
        }

        public static Value.Bool from(int i) {
            return switch (i) {
                case -1 -> BoolImpl.NO_VALUE;
                case 0 -> BoolImpl.FALSE;
                case 1 -> BoolImpl.TRUE;
                default -> throw new UnsupportedOperationException();
            };
        }

        public static Value.Bool from(boolean b) {
            return b ? TRUE : FALSE;
        }

        @Override
        public boolean isFalse() {
            return value == 0;
        }

        @Override
        public boolean hasAValue() {
            return value >= 0;
        }

        @Override
        public Bool or(Bool bool) {
            if (this == NO_VALUE) return bool;
            if (bool == NO_VALUE) return this;
            return value == 1 ? this : bool;
        }

        @Override
        public boolean isTrue() {
            return value == 1;
        }

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeInt(value);
        }

        @Override
        public int compareTo(Value o) {
            if (o instanceof BoolImpl b) {
                return value - b.value;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return switch (value) {
                case -1 -> "<no value>";
                case 0 -> "false";
                case 1 -> "true";
                default -> throw new UnsupportedOperationException();
            };
        }
    }

    static {
        decoderMap.put(BoolImpl.class, (codec, encodedValue) -> BoolImpl.from(codec.decodeInt(encodedValue)));
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
        public static final CommutableData NONE = new ValueImpl.CommutableDataImpl(null, null, null);
        public static final CommutableData BLANK = new ValueImpl.CommutableDataImpl("", "", "");

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeList(List.of(codec.encodeString(seq), codec.encodeString(par),
                    codec.encodeString(multi)));
        }

        @Override
        public boolean isNone() {
            return NONE == this;
        }
    }

    static {
        decoderMap.put(CommutableDataImpl.class, (codec, encodedValue) -> {
            List<Codec.EncodedValue> list = codec.decodeList(encodedValue);
            return new CommutableDataImpl(codec.decodeString(list.get(0)), codec.decodeString(list.get(1)),
                    codec.decodeString(list.get(2)));
        });
    }

    public static class ImmutableImpl implements Immutable {
        private final int value;

        private ImmutableImpl(int value) {
            this.value = value;
        }

        // NO_VALUE for 'null' constant
        public static final ImmutableImpl NO_VALUE = new ImmutableImpl(-1);
        public static final ImmutableImpl MUTABLE = new ImmutableImpl(0);
        public static final ImmutableImpl FINAL_FIELDS = new ImmutableImpl(1);
        public static final ImmutableImpl IMMUTABLE_HC = new ImmutableImpl(2);
        public static final ImmutableImpl IMMUTABLE = new ImmutableImpl(3);

        public static Value.Immutable from(int level) {
            return switch (level) {
                case -1 -> NO_VALUE;
                case 0 -> MUTABLE;
                case 1 -> FINAL_FIELDS;
                case 2 -> IMMUTABLE_HC;
                case 3 -> IMMUTABLE;
                default -> throw new UnsupportedOperationException();
            };
        }

        @Override
        public boolean isAtLeastImmutableHC() {
            return value >= 2;
        }

        @Override
        public boolean isImmutable() {
            return value == 3;
        }

        @Override
        public boolean isMutable() {
            return value == 0;
        }

        @Override
        public Immutable max(Immutable other) {
            if (other == null) return this;
            assert this != NO_VALUE && other != NO_VALUE;
            int otherValue = ((ImmutableImpl) other).value;
            if (value >= otherValue) return this;
            return other;
        }

        @Override
        public Immutable min(Immutable other) {
            if (other == null) return this;
            assert this != NO_VALUE && other != NO_VALUE;
            int otherValue = ((ImmutableImpl) other).value;
            if (value <= otherValue) return this;
            return other;
        }

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeInt(value);
        }

        @Override
        public int compareTo(Value o) {
            if (o instanceof ImmutableImpl i) {
                return value - i.value;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Independent toCorrespondingIndependent() {
            return switch (value) {
                case 0, 1 -> IndependentImpl.DEPENDENT;
                case 2 -> IndependentImpl.INDEPENDENT_HC;
                case 3 -> IndependentImpl.INDEPENDENT;
                default -> throw new UnsupportedOperationException();
            };
        }

        @Override
        public String toString() {
            return switch (value) {
                case -1 -> "<no immutable value>";
                case 0 -> "@Mutable";
                case 1 -> "@FinalFields";
                case 2 -> "@Immutable(hc=true)";
                case 3 -> "@Immutable";
                default -> throw new UnsupportedOperationException();
            };
        }
    }

    static {
        decoderMap.put(ImmutableImpl.class, (codec, encodedValue) -> ImmutableImpl.from(codec.decodeInt(encodedValue)));
    }


    public static class IndependentImpl implements Independent {
        private final int value;

        private IndependentImpl(int value) {
            this.value = value;
        }

        public static final Independent DEPENDENT = new IndependentImpl(0);
        public static final Independent INDEPENDENT_HC = new IndependentImpl(1);
        public static final Independent INDEPENDENT = new IndependentImpl(2);

        public static Value from(int level) {
            return switch (level) {
                case 0 -> DEPENDENT;
                case 1 -> INDEPENDENT_HC;
                case 2 -> INDEPENDENT;
                default -> throw new UnsupportedOperationException();
            };
        }

        @Override
        public boolean isAtLeastIndependentHc() {
            return value > 0;
        }

        @Override
        public boolean isDependent() {
            return value == 0;
        }

        @Override
        public boolean isIndependent() {
            return value == 2;
        }

        @Override
        public boolean isIndependentHc() {
            return value == 1;
        }

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeInt(value);
        }

        @Override
        public int compareTo(Value o) {
            if (o instanceof IndependentImpl i) {
                return value - i.value;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Independent min(Independent other) {
            int otherValue = ((IndependentImpl) other).value;
            return value <= otherValue ? this : other;
        }

        @Override
        public Independent max(Independent other) {
            if (other == null) return this;
            int otherValue = ((IndependentImpl) other).value;
            return value >= otherValue ? this : other;
        }

        @Override
        public String toString() {
            return switch (value) {
                case 0 -> "@Dependent";
                case 1 -> "@Independent(hc=true)";
                case 2 -> "@Independent";
                default -> throw new UnsupportedOperationException();
            };
        }
    }

    static {
        decoderMap.put(IndependentImpl.class, (codec, encodedValue) -> IndependentImpl.from(codec.decodeInt(encodedValue)));
    }

    public record FieldValueImpl(FieldInfo field) implements FieldValue {
        public static final FieldValue EMPTY = new FieldValueImpl(null);

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeInfo(field, -1);
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

    public record GetSetEquivalentImpl(Set<ParameterInfo> convertToGetSet,
                                       MethodInfo methodWithoutParameters) implements GetSetEquivalent {
        public static final GetSetEquivalent EMPTY = new GetSetEquivalentImpl(Set.of(), null);

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            Set<Codec.EncodedValue> set = convertToGetSet.stream().map(codec::encodeVariable)
                    .collect(Collectors.toUnmodifiableSet());
            return codec.encodeList(List.of(codec.encodeSet(set), codec.encodeInfo(methodWithoutParameters)));
        }
    }

    static {
        decoderMap.put(GetSetEquivalentImpl.class, (codec, encodedValue) -> {
            List<Codec.EncodedValue> list = codec.decodeList(encodedValue);
            Set<ParameterInfo> set = codec.decodeSet(list.get(0)).stream()
                    .map(codec::decodeParameterInfo).collect(Collectors.toUnmodifiableSet());
            return new GetSetEquivalentImpl(set, codec.decodeMethodInfo(list.get(1)));
        });
    }


    public record NotNullImpl(int value) implements NotNull {
        public static final NotNull NO_VALUE = new NotNullImpl(-1);
        public static final NotNull NULLABLE = new NotNullImpl(0);
        public static final NotNull NOT_NULL = new NotNullImpl(1);
        public static final NotNull CONTENT_NOT_NULL = new NotNullImpl(2);

        public static Value from(int level) {
            return switch (level) {
                case -1 -> NO_VALUE;
                case 0 -> NULLABLE;
                case 1 -> NOT_NULL;
                case 2 -> CONTENT_NOT_NULL;
                default -> throw new UnsupportedOperationException();
            };
        }

        @Override
        public boolean isAtLeastNotNull() {
            return value >= 1;
        }

        @Override
        public boolean isNullable() {
            return value == 0;
        }

        @Override
        public Codec.EncodedValue encode(Codec codec) {
            return codec.encodeInt(value);
        }

        @Override
        public int compareTo(Value o) {
            if (o instanceof NotNullImpl i) {
                return value - i.value;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public NotNull max(NotNull other) {
            int v = ((NotNullImpl) other).value;
            return value >= v ? this : other;
        }
    }

    static {
        decoderMap.put(NotNullImpl.class, (codec, encodedValue) -> NotNullImpl.from(codec.decodeInt(encodedValue)));
    }

}
