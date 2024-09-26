package org.e2immu.language.cst.api.analysis;

import org.e2immu.language.cst.api.element.Element;
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

    boolean decodeBoolean(EncodedValue encodedValue);

    Expression decodeExpression(EncodedValue value);

    FieldInfo decodeFieldInfo(EncodedValue encodedValue);

    int decodeInt(EncodedValue encodedValue);

    List<EncodedValue> decodeList(EncodedValue encodedValue);

    Map<EncodedValue, EncodedValue> decodeMap(EncodedValue encodedValue);

    MethodInfo decodeMethodInfo(EncodedValue encodedValue);

    ParameterInfo decodeParameterInfo(EncodedValue ev);

    Set<EncodedValue> decodeSet(EncodedValue encodedValue);

    String decodeString(EncodedValue encodedValue);

    EncodedValue encodeBoolean(boolean value);

    EncodedValue encodeExpression(Expression expression);

    default EncodedValue encodeInfo(Info info) {
        return encodeInfo(info, -1);
    }

    EncodedValue encodeInfo(Info info, int index);

    EncodedValue encodeInt(int value);

    EncodedValue encodeList(List<EncodedValue> encodedValues);

    EncodedValue encodeMap(Map<EncodedValue, EncodedValue> map);

    EncodedValue encodeSet(Set<EncodedValue> set);

    EncodedValue encodeString(String string);

    EncodedValue encodeVariable(Variable variable);

    boolean isList(EncodedValue encodedValue);

    record EncodedPropertyValue(String key, EncodedValue encodedValue) {
    }

    record PropertyValue(Property property, Value value) {
    }

    default EncodedPropertyValue encode(Property property, Value value) {
        EncodedValue encodedValue = value.encode(this);
        return new EncodedPropertyValue(property.key(), encodedValue);
    }

    interface DecoderProvider {
        BiFunction<Codec, EncodedValue, Value> decoder(Class<? extends Value> clazz);
    }

    interface TypeProvider {
        TypeInfo get(String fqn);
    }

    // Info level

    interface EncodedValue {
    }

    Stream<PropertyValue> decode(PropertyValueMap pvm, Stream<EncodedPropertyValue> encodedPropertyValueStream);

    EncodedValue encode(Info info, int index, Stream<EncodedPropertyValue> encodedPropertyValueStream);
}

