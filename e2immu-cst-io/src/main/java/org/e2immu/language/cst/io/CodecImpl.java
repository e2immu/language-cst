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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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

    public record E(String s, List<EncodedValue> subs) implements EncodedValue {
        public E(String s) {
            this(s, null);
        }

        @Override
        public String toString() {
            try (StringWriter sw = new StringWriter()) {
                write(sw, Integer.MIN_VALUE, false);
                return sw.toString();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        public void write(Writer writer, int tab, boolean surround) throws IOException {
            if (tab >= -1) {
                writer.write("\n");
                if (tab >= 0) writer.write(" ".repeat(tab));
            }
            if (surround) writer.write("{");
            writer.write(s);
            if (subs != null && !subs.isEmpty()) {
                if (subs.size() == 1) {
                    EncodedValue sub0 = subs.get(0);
                    if (sub0 != null) {
                        writer.write(", \"sub\":");
                        ((E) sub0).write(writer, tab + 1, true);
                    }
                } else {
                    StringWriter sw = new StringWriter();
                    boolean first = true;
                    boolean wrote = false;
                    for (EncodedValue sub : subs) {
                        if (sub != null) {
                            if (first) first = false;
                            else sw.append(",");
                            ((E) sub).write(sw, tab + 1, true);
                            wrote = true;
                        }
                    }
                    if (wrote) {
                        writer.write(", \"subs\":[");
                        writer.write(sw.toString());
                        writer.write("]");
                    }
                }
            }
            if (surround) writer.write("}");
        }
    }

    @Override
    public boolean decodeBoolean(Context context, EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof Literal l) {
            return "true".equals(l.getSource());
        } else throw new UnsupportedOperationException();
    }

    @Override
    public Expression decodeExpression(Context context, EncodedValue value) {
        throw new UnsupportedOperationException(); // not implemented here, need parser and context
    }

    @Override
    public Variable decodeVariable(Context context, EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof StringLiteral sl) {
            String source = unquote(sl.getSource());
            if ('P' == source.charAt(0)) {
                return decodeParameterOutOfContext(context, source.substring(1));
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldInfo decodeFieldInfo(Context context, EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof StringLiteral sl) {
            String source = unquote(sl.getSource());
            assert 'F' == source.charAt(0);
            String rest = source.substring(1);
            return decodeFieldInfo(context, rest);
        }
        throw new UnsupportedOperationException();
    }

    private static final Pattern NAME_INDEX_PATTERN = Pattern.compile("(.+)\\((\\d+)\\)");

    private FieldInfo decodeFieldInfo(Context context, String nameIndex) {
        Matcher m = NAME_INDEX_PATTERN.matcher(nameIndex);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(2));
            FieldInfo fieldInfo = context.currentType().fields().get(index);
            assert fieldInfo.name().equals(m.group(1));
            return fieldInfo;
        } else throw new UnsupportedOperationException();
    }

    private TypeInfo decodeSubType(Context context, String nameIndex) {
        Matcher m = NAME_INDEX_PATTERN.matcher(nameIndex);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(2));
            TypeInfo typeInfo = context.currentType();
            assert typeInfo != null;
            TypeInfo subType = typeInfo.subTypes().get(index);
            assert subType.simpleName().equals(m.group(1));
            return subType;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Info decodeInfo(Context context, char type, String name) {
        return switch (type) {
            case 'F' -> decodeFieldInfo(context, name);
            case 'C' -> decodeConstructor(context, name);
            case 'M' -> decodeMethodInfo(context, name);
            case 'T' -> typeProvider.get(name);
            case 'S' -> decodeSubType(context, name);
            case 'P' -> decodeParameterInfo(context, name);
            default -> throw new UnsupportedOperationException();
        };
    }

    @Override
    public Info decodeInfo(Context context, EncodedValue ev) {
        if (ev instanceof D d && d.s instanceof StringLiteral sl) {
            String source = unquote(sl.getSource());
            char c0 = source.charAt(0);
            String rest = source.substring(1);
            return decodeInfo(context, c0, rest);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int decodeInt(Context context, EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof Literal l) {
            return Integer.parseInt(l.getSource());
        } else throw new UnsupportedOperationException();
    }

    @Override
    public List<EncodedValue> decodeList(Context context, EncodedValue encodedValue) {
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
    public Map<EncodedValue, EncodedValue> decodeMap(Context context, EncodedValue encodedValue) {
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

    private MethodInfo decodeConstructor(Context context, String fqnNameIndex) {
        Matcher m = NAME_INDEX_PATTERN.matcher(fqnNameIndex);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(2));
            MethodInfo methodInfo = context.currentType().constructors().get(index);
            assert methodInfo.isConstructor();
            return methodInfo;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private MethodInfo decodeMethodInfo(Context context, String nameIndex) {
        Matcher m = NAME_INDEX_PATTERN.matcher(nameIndex);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(2));
            MethodInfo methodInfo = context.currentType().methods().get(index);
            assert !methodInfo.isConstructor();
            assert methodInfo.name().equals(m.group(1));
            return methodInfo;
        } else throw new UnsupportedOperationException();
    }

    @Override
    public MethodInfo decodeMethodOutOfContext(Context context, EncodedValue ev) {
        if (ev instanceof D d && d.s instanceof StringLiteral sl) {
            String source = unquote(sl.getSource());
            assert 'M' == source.charAt(0);
            String rest = source.substring(1);
            return decodeMethodOutOfContext(context, rest);
        }
        throw new UnsupportedOperationException();
    }

    private static final Pattern METHOD_PATTERN = Pattern.compile("(.+)\\.(\\w+)\\((\\d+)\\)");

    private MethodInfo decodeMethodOutOfContext(Context context, String name) {
        Matcher m = METHOD_PATTERN.matcher(name);
        if (m.matches()) {
            String typeFqn = m.group(1);
            TypeInfo typeInfo = findType(context, typeFqn);
            int index = Integer.parseInt(m.group(3));
            MethodInfo methodInfo = typeInfo.methods().get(index);
            assert methodInfo.name().equals(m.group(2));
            return methodInfo;
        }
        throw new UnsupportedOperationException();
    }

    private TypeInfo findType(Context context, String fqn) {
        TypeInfo typeInfo = context.findType(fqn);
        if (typeInfo != null) return typeInfo;
        return typeProvider().get(fqn);
    }

    private ParameterInfo decodeParameterOutOfContext(Context context, String name) {
        int colon = name.lastIndexOf(':');
        String method = name.substring(0, colon);
        MethodInfo methodInfo = decodeMethodOutOfContext(context, method);
        int index = Integer.parseInt(name.substring(colon + 1));
        return methodInfo.parameters().get(index);
    }

    private ParameterInfo decodeParameterInfo(Context context, String nameIndex) {
        Matcher m = NAME_INDEX_PATTERN.matcher(nameIndex);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(2));
            ParameterInfo parameterInfo = context.currentMethod().parameters().get(index);
            assert parameterInfo.name().equals(m.group(1));
            return parameterInfo;
        } else throw new UnsupportedOperationException();
    }


    @Override
    public Set<EncodedValue> decodeSet(Context context, EncodedValue encodedValue) {
        Set<EncodedValue> list = new LinkedHashSet<>();
        if (encodedValue instanceof D d && d.s instanceof Array jo && jo.size() > 2) {
            for (int i = 1; i < jo.size(); i += 2) {
                list.add(new D(jo.get(i)));
            }
        }
        return list;
    }

    @Override
    public String decodeString(Context context, EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof StringLiteral l) {
            return unquote(l.getSource());
        } else throw new UnsupportedOperationException();
    }

    @Override
    public EncodedValue encodeBoolean(Context context, boolean value) {
        return new E(Boolean.toString(value));
    }

    @Override
    public EncodedValue encodeExpression(Context context, Expression expression) {
        return new E(quote(expression.toString()));
    }

    @Override
    public EncodedValue encodeInfo(Context context, Info info, String index) {
        return new E(quote(encodeInfoFqn(context, info, index)));
    }

    @Override
    public EncodedValue encodeInt(Context context, int value) {
        return new E(Integer.toString(value));
    }

    @Override
    public EncodedValue encodeList(Context context, List<EncodedValue> encodedValues) {
        String e = encodedValues.stream().map(ev -> ((E) ev).s)
                .collect(Collectors.joining(",", "[", "]"));
        return new E(e);
    }

    @Override
    public EncodedValue encodeMap(Context context, Map<EncodedValue, EncodedValue> map) {
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
    public EncodedValue encodeSet(Context context, Set<EncodedValue> set) {
        String e = set.stream().map(ev -> ((E) ev).s).sorted()
                .collect(Collectors.joining(",", "[", "]"));
        return new E(e);
    }

    @Override
    public EncodedValue encodeString(Context context, String string) {
        return new E(quote(string));
    }

    @Override
    public EncodedValue encodeMethodOutOfContext(Context context, MethodInfo methodInfo) {
        String e = "M" + methodInfo.typeInfo().fullyQualifiedName() + "." + methodInfo.name()
                   + "(" + methodIndex(methodInfo) + ")";
        return new E(quote(e));
    }

    public static String quote(String s) {
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    public static String unquote(String s) {
        String s1 = s.substring(1, s.length() - 1);
        return s1.replace("\\\"", "\"");
    }

    @Override
    public EncodedValue encodeVariable(Context context, Variable variable) {
        String type;
        if (variable instanceof ParameterInfo pi) {
            return new E(quote("P" + pi.typeInfo().fullyQualifiedName() + "." + pi.methodInfo().name()
                               + "(" + methodIndex(pi.methodInfo()) + "):" + pi.index()));
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

    public record DII(Codec codec, Context context) implements DI {
    }

    @Override
    public Stream<PropertyValue> decode(Context context,
                                        PropertyValueMap pvm,
                                        Stream<EncodedPropertyValue> encodedPropertyValueStream) {
        return encodedPropertyValueStream.map(epv -> {
            String key = epv.key();
            Property property = propertyProvider.get(key);
            assert property != null : "Have no property object for key " + key;
            Class<? extends Value> clazz = property.classOfValue();
            BiFunction<DI, EncodedValue, Value> decoder = decoderProvider.decoder(clazz);
            D d = (D) epv.encodedValue();
            Value value = decoder.apply(new DII(this, context), d);
            return new PropertyValue(property, value);
        });
    }

    @Override
    public EncodedValue encode(Context context,
                               Info info,
                               String index,
                               Stream<EncodedPropertyValue> encodedPropertyValueStream,
                               List<EncodedValue> subs) {
        String fqn = encodeInfoFqn(context, info, index);
        String pvStream = encodedPropertyValueStream
                .filter(epv -> epv.encodedValue() != null)
                .map(epv -> '"' + epv.key() + "\":" + ((E) epv.encodedValue()).s)
                .sorted()
                .collect(Collectors.joining(",", "{", "}"));
        if ("{}".equalsIgnoreCase(pvStream)) {
            // no data, we'll not write
            return null;
        }
        String all = "\"name\": " + quote(fqn) + ", \"data\":" + pvStream;
        return new E(all, subs);
    }

    private String encodeInfoFqn(Context context, Info info, String index) {
        if (info instanceof TypeInfo typeInfo) {
            if (typeInfo.isPrimaryType()) {
                return "T" + typeInfo.fullyQualifiedName();
            }
            if (typeInfo.simpleName().startsWith("$")) {
                assert context.currentMethod() != null;
                return "A" + typeInfo.enclosingMethod() + "(" + index + ")";
            }
            return "S" + typeInfo.simpleName() + "(" + index + ")";
        }
        assert index != null && !index.isBlank();
        if (info instanceof MethodInfo methodInfo) {
            if (methodInfo.isConstructor()) {
                return "C<init>(" + index + ")";
            }
            return "M" + methodInfo.name() + "(" + index + ")";
        }
        if (info instanceof FieldInfo fieldInfo) {
            assert context.currentType() != null;
            return "F" + fieldInfo.name() + "(" + index + ")";
        }
        if (info instanceof ParameterInfo pi) {
            assert context.currentMethod() != null;
            return "P" + pi.name() + "(" + index + ")";
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

    public static class ContextImpl implements Context {
        private final Stack<Info> stack = new Stack<>();

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public Info pop() {
            return stack.pop();
        }

        @Override
        public Info peek(int stepsBack) {
            return stack.elementAt(stack.size() - 1 - stepsBack);
        }

        @Override
        public void push(Info info) {
            stack.push(info);
        }

        @Override
        public TypeInfo currentType() {
            for (int i = 0; i < stack.size(); i++) {
                if (peek(i) instanceof TypeInfo ti) return ti;
            }
            return null;
        }

        @Override
        public MethodInfo currentMethod() {
            for (int i = 0; i < stack.size(); i++) {
                if (peek(i) instanceof MethodInfo mi) return mi;
            }
            return null;
        }

        @Override
        public TypeInfo findType(String typeFqn) {
            for (int i = 0; i < stack.size(); i++) {
                if (peek(i) instanceof TypeInfo ti && typeFqn.equals(ti.fullyQualifiedName())) return ti;
            }
            return null;
        }

        @Override
        public boolean methodBeforeType() {
            for (int i = 0; i < stack.size(); i++) {
                Info peek = peek(i);
                if (peek instanceof TypeInfo) return false;
                if (peek instanceof MethodInfo) return true;
            }
            return false;
        }
    }
}
