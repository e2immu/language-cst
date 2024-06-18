package org.e2immu.language.cst.io;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.variable.*;
import org.parsers.json.Node;
import org.parsers.json.ast.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodecImpl implements Codec {
    private final DecoderProvider decoderProvider;

    public CodecImpl(DecoderProvider decoderProvider) {
        this.decoderProvider = decoderProvider;
    }

    record D(Node s) implements EncodedValue {
    }

    record E(String s) implements EncodedValue {
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
    public FieldInfo decodeFieldInfo(EncodedValue encodedValue) {
        throw new UnsupportedOperationException(); // not implemented here, need type context
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

    @Override
    public MethodInfo decodeMethodInfo(EncodedValue encodedValue) {
        throw new UnsupportedOperationException(); // not implemented here, need type context
    }

    @Override
    public ParameterInfo decodeParameterInfo(EncodedValue ev) {
        throw new UnsupportedOperationException(); // not implemented here, need type context
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
    public EncodedValue encodeInfo(Info info) {
        return new E(quote(encodeInfoFqn(info)));
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
        String encoded = map.entrySet().stream().map(e -> ((E) e.getKey()).s + ":" + ((E) e.getValue()).s)
                .collect(Collectors.joining(",", "{", "}"));
        return new E(encoded);
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
            Property property = pvm.property(key);
            Class<? extends Value> clazz = property.classOfValue();
            BiFunction<Codec, EncodedValue, Value> decoder = decoderProvider.decoder(clazz);
            D d = (D) epv.encodedValue();
            Value value = decoder.apply(this, d);
            return new PropertyValue(property, value);
        });
    }

    @Override
    public EncodedValue encode(Element info, Stream<EncodedPropertyValue> encodedPropertyValueStream) {
        String fqn;
        if (info instanceof Info i) {
            fqn = encodeInfoFqn(i);
        } else if (info instanceof ParameterInfo pi) {
            fqn = "P" + pi.fullyQualifiedName();
        } else throw new UnsupportedOperationException();
        String pvStream = encodedPropertyValueStream.map(epv -> '"' + epv.key() + "\":" + ((E) epv.encodedValue()).s)
                .collect(Collectors.joining(",", "{", "}"));
        String all = "{\"fqn\": " + quote(fqn) + ", \"data\":" + pvStream + "}";
        return new E(all);
    }

    private String encodeInfoFqn(Info info) {
        if (info instanceof TypeInfo typeInfo) {
            return "T" + typeInfo.fullyQualifiedName();
        }
        if (info instanceof MethodInfo methodInfo) {
            return "M" + methodInfo.fullyQualifiedName();
        }
        if (info instanceof FieldInfo fieldInfo) {
            return "F" + fieldInfo.fullyQualifiedName();
        }
        if (info instanceof ParameterInfo pi) {
            return "P" + pi.fullyQualifiedName();
        }
        throw new UnsupportedOperationException();
    }
}
