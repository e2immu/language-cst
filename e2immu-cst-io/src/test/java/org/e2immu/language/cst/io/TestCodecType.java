package org.e2immu.language.cst.io;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.element.CompilationUnit;
import org.e2immu.language.cst.api.expression.EnclosedExpression;
import org.e2immu.language.cst.api.expression.VariableExpression;
import org.e2immu.language.cst.api.info.FieldInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.FieldReference;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
import org.e2immu.language.cst.impl.analysis.PropertyProviderImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.e2immu.language.cst.impl.runtime.RuntimeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.parsers.json.JSONLexer;
import org.parsers.json.JSONParser;
import org.parsers.json.Node;
import org.parsers.json.Token;
import org.parsers.json.ast.JSONObject;
import org.parsers.json.ast.KeyValuePair;
import org.parsers.json.ast.Root;
import org.parsers.json.ast.StringLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class TestCodecType extends  CommonTest {

    @DisplayName("int")
    @Test
    public void test1() {
        String encoded = "\"Tint\"";
        assertEquals(encoded, codec.encodeType(context, runtime.intParameterizedType()).toString());
        CodecImpl.D d = makeD(encoded);
        assertEquals(runtime.intParameterizedType(), codec.decodeType(context, d));
    }

    @DisplayName("int[]")
    @Test
    public void test2() {
        String encoded = "[\"Tint\",1,[]]";
        ParameterizedType type = runtime.intParameterizedType().copyWithArrays(1);
        assertEquals(encoded, codec.encodeType(context, type).toString());

        CodecImpl.D d = makeD(encoded);
        assertEquals(type, codec.decodeType(context, d));
    }

    @DisplayName("String<int, int>")
    @Test
    public void test3() {
        String encoded = "[\"Tjava.lang.String\",0,[\"Tint\",\"Tint\"]]";
        ParameterizedType type = runtime.newParameterizedType(runtime.stringTypeInfo(),
                List.of(runtime.intParameterizedType(), runtime.intParameterizedType()));
        assertEquals(encoded, codec.encodeType(context, type).toString());
        CodecImpl.D d = makeD(encoded);
        assertEquals(type, codec.decodeType(context, d));
    }

    @DisplayName("unbound wildcard ?")
    @Test
    public void test4() {
        String encoded = "\"?\"";
        ParameterizedType type = runtime.parameterizedTypeWildcard();
        assertEquals("?", type.toString());
        assertEquals(encoded, codec.encodeType(context, type).toString());
        CodecImpl.D d = makeD(encoded);
        assertEquals(type, codec.decodeType(context, d));
    }

    @DisplayName("? super String")
    @Test
    public void test5() {
        String encoded = "[\"Tjava.lang.String\",0,[],\"S\"]";
        ParameterizedType type = runtime.newParameterizedType(runtime.stringTypeInfo(), 0, runtime.wildcardSuper(), List.of());
        assertEquals("Type ? super String", type.toString());
        assertEquals(encoded, codec.encodeType(context, type).toString());
        CodecImpl.D d = makeD(encoded);
        assertEquals(type, codec.decodeType(context, d));
    }
}
