package org.e2immu.language.cst.api.analysis;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/*
support for reading and writing the property-value pairs in many elements.
 */
public interface Codec {
    interface Context {
        TypeInfo findType(Codec.TypeProvider typeProvider, String typeFqn);

        boolean isEmpty();

        boolean methodBeforeType();

        Info pop();

        Info peek(int stepsBack);

        void push(Info info);

        TypeInfo currentType();

        MethodInfo currentMethod();
    }

    boolean decodeBoolean(Context context, EncodedValue encodedValue);

    Variable decodeVariable(Context context, EncodedValue encodedValue);

    Expression decodeExpression(Context context, EncodedValue value);

    FieldInfo decodeFieldInfo(Context context, EncodedValue encodedValue);

    Info decodeInfo(Context context, char type, String name);

    Info decodeInfo(Context context, EncodedValue ev);

    int decodeInt(Context context, EncodedValue encodedValue);

    List<EncodedValue> decodeList(Context context, EncodedValue encodedValue);

    Map<EncodedValue, EncodedValue> decodeMap(Context context, EncodedValue encodedValue);

    MethodInfo decodeMethodOutOfContext(Context context, EncodedValue encodedValue);

    Set<EncodedValue> decodeSet(Context context, EncodedValue encodedValue);

    String decodeString(Context context, EncodedValue encodedValue);

    EncodedValue encodeBoolean(Context context, boolean value);

    EncodedValue encodeExpression(Context context, Expression expression);

    EncodedValue encodeInfo(Context context, Info info, String index);

    EncodedValue encodeInt(Context context, int value);

    EncodedValue encodeList(Context context, List<EncodedValue> encodedValues);

    EncodedValue encodeMap(Context context, Map<EncodedValue, EncodedValue> map);

    EncodedValue encodeSet(Context context, Set<EncodedValue> set);

    EncodedValue encodeString(Context context, String string);

    EncodedValue encodeVariable(Context context, Variable variable);

    EncodedValue encodeMethodOutOfContext(Context context, MethodInfo methodInfo);

    int fieldIndex(FieldInfo key);

    int methodIndex(MethodInfo methodInfo);

    int constructorIndex(MethodInfo methodInfo);

    boolean isList(EncodedValue encodedValue);

    interface DI {
        Codec codec();

        Context context();
    }

    record EncodedPropertyValue(String key, EncodedValue encodedValue) {
    }

    record PropertyValue(Property property, Value value) {
    }

    default EncodedPropertyValue encode(Context context, Property property, Value value) {
        EncodedValue encodedValue = value.encode(this, context);
        return new EncodedPropertyValue(property.key(), encodedValue);
    }

    interface DecoderProvider {
        BiFunction<DI, EncodedValue, Value> decoder(Class<? extends Value> clazz);
    }

    interface TypeProvider {
        TypeInfo get(String fqn);
    }

    interface PropertyProvider {
        Property get(String propertyName);
    }

    // for extensions
    TypeProvider typeProvider();

    PropertyProvider propertyProvider();

    // Info level

    interface EncodedValue {
    }

    Stream<PropertyValue> decode(Context context, PropertyValueMap pvm, Stream<EncodedPropertyValue> encodedPropertyValueStream);

    EncodedValue encode(Context context, Info info, String index, Stream<EncodedPropertyValue> encodedPropertyValueStream,
                        List<EncodedValue> subs);
}

