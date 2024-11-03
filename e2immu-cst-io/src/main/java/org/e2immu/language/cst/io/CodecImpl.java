package org.e2immu.language.cst.io;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.type.NamedType;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.type.TypeParameter;
import org.e2immu.language.cst.api.type.Wildcard;
import org.e2immu.language.cst.api.variable.*;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
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
    private final Runtime runtime;

    public CodecImpl(Runtime runtime, PropertyProvider propertyProvider, DecoderProvider decoderProvider, TypeProvider typeProvider) {
        this.decoderProvider = decoderProvider;
        this.typeProvider = typeProvider;
        this.propertyProvider = propertyProvider;
        this.runtime = runtime;
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
        return new ExpressionCodec(runtime, this, context).decodeExpression(value);
    }

    @Override
    public Variable decodeVariable(Context context, EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof StringLiteral sl) {
            String source = unquote(sl.getSource());
            char variableType = source.charAt(0);
            return switch (variableType) {
                case 'P' -> decodeParameterOutOfContext(context, source.substring(1));
                case 'F' -> decodeFieldReference(context, source.substring(1));
                default -> throw new UnsupportedOperationException();
            };
        }
        throw new UnsupportedOperationException();
    }

    private FieldReference decodeFieldReference(Context context, String fqn) {
        assert !fqn.contains("#") : "TODO not yet implemented";

        int lastDot = fqn.lastIndexOf('.');
        String typeFqn = fqn.substring(0, lastDot);
        TypeInfo typeInfo = context.findType(typeProvider, typeFqn);
        String fieldName = fqn.substring(lastDot + 1);
        FieldInfo fieldInfo = typeInfo.getFieldByName(fieldName, true);
        return runtime.newFieldReference(fieldInfo);
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
            return Integer.parseInt(potentiallyUnquote(l.getSource()));
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
            TypeInfo typeInfo = context.findType(typeProvider, typeFqn);
            int index = Integer.parseInt(m.group(3));
            MethodInfo methodInfo = typeInfo.methods().get(index);
            assert methodInfo.name().equals(m.group(2));
            return methodInfo;
        }
        throw new UnsupportedOperationException();
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
            return context.currentMethod().parameters().get(index);
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
        return new ExpressionCodec(runtime, this, context).encodeExpression(expression);
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

    public static String potentiallyUnquote(String s) {
        if (!s.isEmpty() && s.charAt(0) == '"') return unquote(s);
        return s;
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
    public void decode(Context context,
                       PropertyValueMap pvm,
                       Stream<EncodedPropertyValue> encodedPropertyValueStream) {
        encodedPropertyValueStream.map(epv -> {
            String key = epv.key();
            Property property = propertyProvider.get(key);
            assert property != null : "Have no property object for key " + key;
            Class<? extends Value> clazz = property.classOfValue();
            BiFunction<DI, EncodedValue, Value> decoder = decoderProvider.decoder(clazz);
            D d = (D) epv.encodedValue();
            Value value = decoder.apply(new DII(this, context), d);
            return new PropertyValue(property, value);
        }).forEach(pv -> {
            // the GET_SET_FIELD property can already be set (by GetSetUtil) at byte-code loading
            if (!pv.property().equals(PropertyImpl.GET_SET_FIELD) || !pvm.haveAnalyzedValueFor(PropertyImpl.GET_SET_FIELD)) {
                pvm.set(pv.property(), pv.value());
            }
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

    @Override
    public EncodedValue encodeType(Context context, ParameterizedType type) {
        if (type == null) return encodeString(context, "-");
        String s0;
        if (type.typeInfo() != null) {
            s0 = "T" + encodeString(context, type.fullyQualifiedName());
        } else if (type.typeParameter() != null) {
            s0 = (type.typeParameter().isMethodTypeParameter() ? "M" : "P") + type.typeParameter().getIndex();
        } else {
            s0 = type.isUnboundWildcard() ? "?" : "X";
        }
        EncodedValue e0 = encodeString(context, s0);
        EncodedValue e1 = type.isTypeParameter() ? encodeOwnerOfTypeParameter(context, type.typeParameter()) : null;
        if (type.arrays() == 0 && type.parameters().isEmpty() && e1 == null) {
            return e0;
        }
        EncodedValue e2 = encodeInt(context, type.arrays());
        Stream<EncodedValue> s3 = type.parameters().stream().map(p -> encodeType(context, p));
        EncodedValue e4 = encodeWildcard(context, type.wildcard());
        return encodeList(context, Stream.concat(Stream.concat(Stream.concat(
                Stream.concat(Stream.of(e0), Stream.ofNullable(e1)), Stream.of(e2)), s3), Stream.ofNullable(e4)).toList());
    }

    private EncodedValue encodeOwnerOfTypeParameter(Context context, TypeParameter typeParameter) {
        if (typeParameter.isMethodTypeParameter()) {
            MethodInfo methodInfo = typeParameter.getOwner().getRight();
            if (methodInfo.equals(context.currentMethod())) return null;
            return encodeInfo(context, methodInfo, "" + methodIndex(methodInfo));
        }
        TypeInfo typeInfo = typeParameter.getOwner().getLeft();
        if (typeInfo.equals(context.currentType())) return null;
        return encodeInfo(context, typeInfo, null);
    }

    private EncodedValue encodeWildcard(Context context, Wildcard wildcard) {
        if (wildcard == null || wildcard.isUnbound()) return null;
        String s;
        if (wildcard.isExtends()) s = "E";
        else if (wildcard.isSuper()) s = "S";
        else throw new UnsupportedOperationException();
        return encodeString(context, s);
    }

    @Override
    public ParameterizedType decodeType(Context context, EncodedValue encodedValue) {
        if (encodedValue instanceof D d && d.s instanceof StringLiteral sl) {
            return decodeSimpleType(context, sl);
        }
        List<EncodedValue> list = decodeList(context, encodedValue);

        // list index 0: named type
        int i = 1;
        NamedType nt;
        if (list.get(0) instanceof D d0 && d0.s instanceof StringLiteral sl) {
            String fqn = unquote(sl.getSource());
            char first = fqn.charAt(0);
            if ('T' == first) {
                nt = context.findType(typeProvider, fqn.substring(1));
            } else {
                int index = Integer.parseInt(fqn.substring(1));
                boolean ownerNotInContext = !(list.get(i) instanceof D d1 && d1.s instanceof NumberLiteral);
                if ('M' == first) {
                    MethodInfo owner = ownerNotInContext ? (MethodInfo) decodeInfo(context, list.get(i)) : context.currentMethod();
                    nt = owner.typeParameters().get(index);
                } else if ('P' == first) {
                    TypeInfo owner = ownerNotInContext ? (TypeInfo) decodeInfo(context, list.get(i)) : context.currentType();
                    nt = owner.typeParameters().get(index);
                } else throw new UnsupportedOperationException();
                if (ownerNotInContext) {
                    ++i;
                }
            }
        } else throw new UnsupportedOperationException();

        // list index 2: arrays
        int arrays;
        if (list.get(i) instanceof D d2 && d2.s instanceof NumberLiteral nl) {
            arrays = Integer.parseInt(nl.getSource());
        } else throw new UnsupportedOperationException();

        // list index 3: type parameters
        ++i;
        List<ParameterizedType> parameters = decodeList(context, list.get(i)).stream().map(ev -> decodeType(context, ev)).toList();

        // list index 4, optional: wildcard
        Wildcard wildCard;
        ++i;
        if (list.size() <= i) {
            wildCard = null;
        } else if (list.get(i) instanceof D d3 && d3.s instanceof StringLiteral sl2) {
            String s = unquote(sl2.getSource());
            wildCard = "E".equals(s) ? runtime.wildcardExtends() : runtime.wildcardSuper();
        } else throw new UnsupportedOperationException();

        // create
        if (nt instanceof TypeInfo ti) {
            return runtime.newParameterizedType(ti, arrays, wildCard, parameters);
        }
        if (nt instanceof TypeParameter tp) {
            return runtime.newParameterizedType(tp, arrays, wildCard);
        }
        throw new UnsupportedOperationException();
    }

    private ParameterizedType decodeSimpleType(Context context, StringLiteral sl) {
        String fqn = unquote(sl.getSource());
        char first = fqn.charAt(0);
        return switch (first) {
            case '-' -> null;
            case 'X' -> runtime.parameterizedTypeNullConstant();
            case '?' -> runtime.parameterizedTypeWildcard();
            case 'T' -> context.findType(typeProvider, fqn.substring(1)).asSimpleParameterizedType();
            case 'M' -> {
                // method type parameter in current context
                int index = Integer.parseInt(fqn.substring(1));
                yield context.currentMethod().typeParameters().get(index).asParameterizedType();
            }
            case 'P' -> {
                // type parameter in current context
                int index = Integer.parseInt(fqn.substring(1));
                yield context.currentType().typeParameters().get(index).asParameterizedType();
            }
            default -> throw new UnsupportedOperationException("TODO: " + fqn);
        };
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
        public TypeInfo findType(Codec.TypeProvider typeProvider, String typeFqn) {
            for (int i = 0; i < stack.size(); i++) {
                if (peek(i) instanceof TypeInfo ti && typeFqn.equals(ti.fullyQualifiedName())) return ti;
            }
            return typeProvider.get(typeFqn);
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
