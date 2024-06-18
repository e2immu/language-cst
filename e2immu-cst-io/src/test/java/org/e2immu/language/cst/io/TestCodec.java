package org.e2immu.language.cst.io;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.element.CompilationUnit;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.e2immu.language.cst.impl.runtime.RuntimeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.parsers.json.JSONParser;
import org.parsers.json.Node;
import org.parsers.json.ast.JSONObject;
import org.parsers.json.ast.KeyValuePair;
import org.parsers.json.ast.Root;
import org.parsers.json.ast.StringLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class TestCodec {
    private final Runtime runtime = new RuntimeImpl();

    @Test
    public void test() {
        CompilationUnit cu = runtime.newCompilationUnitBuilder().setPackageName("a.b").build();
        TypeInfo typeInfo = runtime.newTypeInfo(cu, "C");

        typeInfo.analysis().set(PropertyImpl.IMMUTABLE_TYPE, new ValueImpl.ImmutableImpl(3));
        typeInfo.analysis().set(PropertyImpl.SHALLOW_ANALYZER, ValueImpl.BoolImpl.TRUE);
        typeInfo.analysis().set(PropertyImpl.COMMUTABLE_METHODS,
                new ValueImpl.CommutableDataImpl("p1", "p2,p3", "p4"));

        Codec.DecoderProvider decoderProvider = ValueImpl::decoder;
        Codec codec = new CodecImpl(decoderProvider);
        List<Property> properties = List.of(
                PropertyImpl.IMMUTABLE_TYPE,
                PropertyImpl.SHALLOW_ANALYZER,
                PropertyImpl.COMMUTABLE_METHODS);
        Stream<Codec.EncodedPropertyValue> epvStream = properties.stream().map(p ->
                codec.encode(p, typeInfo.analysis().getOrDefault(p, p.defaultValue())));
        String s = ((CodecImpl.E) codec.encode(typeInfo, epvStream)).s();
        assertEquals("""
                {"fqn": "Ta.b.C", "data":{"immutableType":3,"shallowAnalyzer":true,\
                "commutableMethods":["p1","p2,p3","p4"]}}\
                """, s);
        JSONParser parser = new JSONParser(s);
        parser.Root();
        Node root = parser.rootNode();
        assertInstanceOf(Root.class, root);

        TypeInfo typeInfo2 = runtime.newTypeInfo(cu, "D");

        if (root.get(0) instanceof JSONObject jo) {
            if (jo.get(1) instanceof KeyValuePair kvp) {
                if (kvp.get(0) instanceof StringLiteral sl) {
                    assertEquals("\"fqn\"", sl.getSource());
                } else fail();
                if (kvp.get(2) instanceof StringLiteral sl) {
                    assertEquals("\"Ta.b.C\"", sl.getSource());
                } else fail();
            } else fail();
            if (jo.get(3) instanceof KeyValuePair kvp) {
                if (kvp.get(0) instanceof StringLiteral sl) {
                    assertEquals("\"data\"", sl.getSource());
                } else fail();
                if (kvp.get(2) instanceof JSONObject jo2) {
                    List<Codec.EncodedPropertyValue> epvs = new ArrayList<>();
                    for (int i = 1; i < jo2.size(); i += 2) {
                        if (jo2.get(i) instanceof KeyValuePair kvp2) {
                            String key = CodecImpl.unquote(kvp2.get(0).getSource());
                            epvs.add(new Codec.EncodedPropertyValue(key, new CodecImpl.D(kvp2.get(2))));
                        }
                    }
                    List<Codec.PropertyValue> pvs = codec.decode(typeInfo2.analysis(), epvs.stream()).toList();
                    assertEquals(3, pvs.size());
                    pvs.forEach(pv -> typeInfo2.analysis().set(pv.property(), pv.value()));
                } else fail();
            } else fail();
        } else fail();

        Assertions.assertEquals("p2,p3", typeInfo2.analysis().getOrDefault(PropertyImpl.COMMUTABLE_METHODS, ValueImpl.CommutableDataImpl.BLANK).par());
    }
}
