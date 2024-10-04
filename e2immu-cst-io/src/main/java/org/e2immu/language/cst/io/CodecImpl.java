package org.e2immu.language.cst.io;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.variable.*;
import org.parsers.json.Node;
import org.parsers.json.ast.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodecImpl implements Codec {
    private final DecoderProvider decoderProvider;
    private final TypeProvider typeProvider;
    private final PropertyProvider propertyProvider;

    public CodecImpl(PropertyProvider propertyProvider, DecoderProvider decoderProvider, TypeProvider typeProvider) {
        this.decoderProvider = decoderProvider;
        this.typeProvider = typeProvider;
        this.propertyProvider = propertyProvider;
    }

    @Override
    public TypeProvider typeProvider() {
        return typeProvider;
    }

    @Override
    public PropertyProvider propertyProvider() {
        return propertyProvider;
    }

    public record D(Node s) implements EncodedValue {
    }

    record E(String s) implements EncodedValue {
        @Override
        public String toString() {
            return s;
        }
    }

    @Override
    public boolean decodeBoolean(EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof Literal l) {
            return "true".equals(l.getSource());
        } else throw new UnsupportedOperationException();
    }

    @Override
    public Expression decodeExpression(EncodedValue value) {
        throw new UnsupportedOperationException(); // not implemented here, need parser and context
    }

    @Override
    public Variable decodeVariable(EncodedValue encodedValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldInfo decodeFieldInfo(EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof StringLiteral sl) {
            String source = sl.getSource();
            assert 'F' == source.charAt(0);
            String rest = source.substring(1);
            return decodeFieldInfo(rest);
        }
        throw new UnsupportedOperationException();
    }

    private static final Pattern INDEX_NAME_PATTERN = Pattern.compile("(.+)\\.([\\w$<>]+)\\((\\d+)\\)");

    private FieldInfo decodeFieldInfo(String fqnIndex) {
        Matcher m = INDEX_NAME_PATTERN.matcher(fqnIndex);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(3));
            FieldInfo fieldInfo = typeProvider.get(m.group(1)).fields().get(index);
            assert fieldInfo.name().equals(m.group(2));
            return fieldInfo;
        } else throw new UnsupportedOperationException();
    }


    @Override
    public Info decodeInfo(EncodedValue ev) {
        if (ev instanceof D d && d.s instanceof StringLiteral sl) {
            String source = unquote(sl.getSource());
            char c0 = source.charAt(0);
            String rest = source.substring(1);
            return switch (c0) {
                case 'F' -> decodeFieldInfo(rest);
                case 'C' -> decodeConstructor(rest);
                case 'M' -> decodeMethodInfo(rest);
                case 'T' -> typeProvider.get(rest);
                case 'P' -> decodeParameterInfo(ev);
                default -> throw new UnsupportedOperationException("String is " + sl.getSource());
            };
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeInfo decodeTypeinfo(EncodedValue ev) {
        if (ev instanceof D d && d.s instanceof StringLiteral sl) {
            String source = sl.getSource();
            assert 'T' == source.charAt(0);
            String rest = source.substring(1);
            return typeProvider.get(rest);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int decodeInt(EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof Literal l) {
            return Integer.parseInt(l.getSource());
        } else throw new UnsupportedOperationException();
    }

    @Override
    public List<EncodedValue> decodeList(EncodedValue encodedValue) {
        List<EncodedValue> list = new LinkedList<>();
        if (encodedValue instanceof D d && d.s instanceof Array jo && jo.size() > 2) {
            for (int i = 1; i < jo.size(); i += 2) {
                list.add(new D(jo.get(i)));
            }
        }
        return list;
    }

    @Override
    public boolean isList(EncodedValue encodedValue) {
        if (encodedValue instanceof D d) {
            return d.s instanceof Array;
        }
        throw new UnsupportedOperationException("Expect D object, not E object");
    }

    @Override
    public Map<EncodedValue, EncodedValue> decodeMap(EncodedValue encodedValue) {
        Map<EncodedValue, EncodedValue> map = new LinkedHashMap<>();
        if (encodedValue instanceof D d && d.s instanceof JSONObject jo && jo.size() > 2) {
            // kv pairs, starting with 1 unless empty
            for (int i = 1; i < jo.size(); i += 2) {
                if (jo.get(i) instanceof KeyValuePair kvp) {
                    map.put(new D(kvp.get(0)), new D(kvp.get(2)));
                } else throw new UnsupportedOperationException();
            }
        }
        return map;
    }

    private MethodInfo decodeConstructor(String fqnNameIndex) {
        Matcher m = INDEX_NAME_PATTERN.matcher(fqnNameIndex);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(3));
            MethodInfo methodInfo = typeProvider.get(m.group(1)).constructors().get(index);
            assert methodInfo.isConstructor();
            return methodInfo;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private MethodInfo decodeMethodInfo(String fqnNameIndex) {
        Matcher m = INDEX_NAME_PATTERN.matcher(fqnNameIndex);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(3));
            MethodInfo methodInfo = typeProvider.get(m.group(1)).methods().get(index);
            assert !methodInfo.isConstructor();
            assert methodInfo.name().equals(m.group(2));
            return methodInfo;
        } else throw new UnsupportedOperationException();
    }

    // FIXME this method does not work by index
    @Override
    public MethodInfo decodeMethodInfo(EncodedValue ev) {
        if (ev instanceof D d && d.s instanceof StringLiteral sl) {
            String paramFqn = unquote(sl.getSource());
            int open = paramFqn.indexOf('(');
            String preOpen = paramFqn.substring(0, open);
            int dot = preOpen.lastIndexOf('.');
            String typeFqn = preOpen.substring(0, dot);
            String methodName = preOpen.substring(dot + 1);
            TypeInfo typeInfo = typeProvider.get(typeFqn);
            if ("<init>".equals(methodName)) {
                return typeInfo.constructors().stream().filter(c -> paramFqn.equals(c.fullyQualifiedName()))
                        .findFirst().orElseThrow();
            }
            return typeInfo.methods().stream().filter(c -> paramFqn.equals(c.fullyQualifiedName()))
                    .findFirst().orElseThrow();
        }
        throw new UnsupportedOperationException(); // not implemented here, need type context
    }

    // FIXME not by index
    @Override
    public ParameterInfo decodeParameterInfo(EncodedValue ev) {
        if (ev instanceof D d && d.s instanceof StringLiteral sl) {
            String paramFqn = unquote(sl.getSource()).substring(1);
            int open = paramFqn.indexOf('(');
            String preOpen = paramFqn.substring(0, open);
            int dot = preOpen.lastIndexOf('.');
            String typeFqn = preOpen.substring(0, dot);
            String methodName = preOpen.substring(dot + 1);
            TypeInfo typeInfo = typeProvider.get(typeFqn);
            MethodInfo methodInfo;
            int colon = paramFqn.lastIndexOf(':');
            String without1Colon = paramFqn.substring(0, colon);
            int colon2 = without1Colon.lastIndexOf(':');
            String methodFqn = paramFqn.substring(0, colon2);
            if ("<init>".equals(methodName)) {
                methodInfo = typeInfo.constructors().stream().filter(c -> methodFqn.equals(c.fullyQualifiedName()))
                        .findFirst().orElseThrow();
            } else {
                methodInfo = typeInfo.methods().stream().filter(c -> methodFqn.equals(c.fullyQualifiedName()))
                        .findFirst().orElseThrow();
            }
            int paramIndex = Integer.parseInt(paramFqn.substring(colon2 + 1, colon));
            return methodInfo.parameters().get(paramIndex);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<EncodedValue> decodeSet(EncodedValue encodedValue) {
        Set<EncodedValue> list = new LinkedHashSet<>();
        if (encodedValue instanceof D d && d.s instanceof Array jo && jo.size() > 2) {
            for (int i = 1; i < jo.size(); i += 2) {
                list.add(new D(jo.get(i)));
            }
        }
        return list;
    }

    @Override
    public String decodeString(EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof StringLiteral l) {
            return unquote(l.getSource());
        } else throw new UnsupportedOperationException();
    }

    @Override
    public EncodedValue encodeBoolean(boolean value) {
        return new E(Boolean.toString(value));
    }

    @Override
    public EncodedValue encodeExpression(Expression expression) {
        return new E(quote(expression.toString()));
    }

    @Override
    public EncodedValue encodeInfo(Info info, int index) {
        return new E(quote(encodeInfoFqn(info, index)));
    }

    @Override
    public EncodedValue encodeInt(int value) {
        return new E(Integer.toString(value));
    }

    @Override
    public EncodedValue encodeList(List<EncodedValue> encodedValues) {
        String e = encodedValues.stream().map(ev -> ((E) ev).s)
                .collect(Collectors.joining(",", "[", "]"));
        return new E(e);
    }

    @Override
    public EncodedValue encodeMap(Map<EncodedValue, EncodedValue> map) {
        String encoded = map.entrySet().stream()
                .map(e -> quoteNumber(((E) e.getKey()).s) + ":" + ((E) e.getValue()).s)
                .sorted()
                .collect(Collectors.joining(",", "{", "}"));
        return new E(encoded);
    }

    private static final Pattern NUM_PATTERN = Pattern.compile("-?\\.?[0-9]+");

    private static String quoteNumber(String s) {
        if (NUM_PATTERN.matcher(s).matches()) return quote(s);
        return s;
    }

    @Override
    public EncodedValue encodeSet(Set<EncodedValue> set) {
        String e = set.stream().map(ev -> ((E) ev).s).sorted()
                .collect(Collectors.joining(",", "[", "]"));
        return new E(e);
    }

    @Override
    public EncodedValue encodeString(String string) {
        return new E(quote(string));
    }

    public static String quote(String s) {
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    public static String unquote(String s) {
        String s1 = s.substring(1, s.length() - 1);
        return s1.replace("\\\"", "\"");
    }

    @Override
    public EncodedValue encodeVariable(Variable variable) {
        String type;
        if (variable instanceof ParameterInfo) {
            type = "P";
        } else if (variable instanceof FieldReference) {
            type = "F";
        } else if (variable instanceof DependentVariable) {
            type = "D";
        } else if (variable instanceof LocalVariable) {
            type = "L";
        } else if (variable instanceof This) {
            type = "T";
        } else throw new UnsupportedOperationException();
        return new E(quote(type + variable.fullyQualifiedName()));
    }

    @Override
    public Stream<PropertyValue> decode(PropertyValueMap pvm, Stream<EncodedPropertyValue> encodedPropertyValueStream) {
        return encodedPropertyValueStream.map(epv -> {
            String key = epv.key();
            Property property = propertyProvider.get(key);
            assert property != null : "Have no property object for key " + key;
            Class<? extends Value> clazz = property.classOfValue();
            BiFunction<Codec, EncodedValue, Value> decoder = decoderProvider.decoder(clazz);
            D d = (D) epv.encodedValue();
            Value value = decoder.apply(this, d);
            return new PropertyValue(property, value);
        });
    }

    @Override
    public EncodedValue encode(Info info, int index, Stream<EncodedPropertyValue> encodedPropertyValueStream) {
        String fqn = encodeInfoFqn(info, index);
        String pvStream = encodedPropertyValueStream
                .filter(epv -> epv.encodedValue() != null)
                .map(epv -> '"' + epv.key() + "\":" + ((E) epv.encodedValue()).s)
                .sorted()
                .collect(Collectors.joining(",", "{", "}"));
        if ("{}".equalsIgnoreCase(pvStream)) {
            // no data, we'll not write
            return null;
        }
        String all = "{\"fqn\": " + quote(fqn) + ", \"data\":" + pvStream + "}";
        return new E(all);
    }

    private String encodeInfoFqn(Info info, int index) {
        if (info instanceof TypeInfo typeInfo) {
            return "T" + typeInfo.fullyQualifiedName();
        }
        assert index >= 0;
        if (info instanceof MethodInfo methodInfo) {
            if (methodInfo.isConstructor()) {
                return "C" + methodInfo.typeInfo().fullyQualifiedName() + "(" + index + ")";
            }
            return "M" + methodInfo.typeInfo().fullyQualifiedName() + "." + methodInfo.name() + "(" + index + ")";
        }
        if (info instanceof FieldInfo fieldInfo) {
            return "F" + fieldInfo.fullyQualifiedName() + "(" + index + ")";
        }
        if (info instanceof ParameterInfo pi) {
            return "P" + encodeInfoFqn(pi.methodInfo(), index) + ":" + pi.index();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int fieldIndex(FieldInfo fieldInfo) {
        int i = 0;
        for (FieldInfo fi : fieldInfo.owner().fields()) {
            if (fi == fieldInfo) return i;
            ++i;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int methodIndex(MethodInfo methodInfo) {
        int i = 0;
        for (MethodInfo mi : methodInfo.typeInfo().methods()) {
            if (mi == methodInfo) return i;
            ++i;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int constructorIndex(MethodInfo methodInfo) {
        int i = 0;
        for (MethodInfo mi : methodInfo.typeInfo().constructors()) {
            if (mi == methodInfo) return i;
            ++i;
        }
        throw new UnsupportedOperationException();
    }
}
