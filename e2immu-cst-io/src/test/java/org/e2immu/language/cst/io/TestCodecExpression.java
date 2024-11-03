package org.e2immu.language.cst.io;

import org.e2immu.language.cst.api.analysis.Codec;
import org.e2immu.language.cst.api.element.CompilationUnit;
import org.e2immu.language.cst.api.expression.EnclosedExpression;
import org.e2immu.language.cst.api.expression.VariableExpression;
import org.e2immu.language.cst.api.info.FieldInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.FieldReference;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.impl.analysis.PropertyProviderImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.e2immu.language.cst.impl.runtime.RuntimeImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.parsers.json.JSONParser;
import org.parsers.json.Node;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestCodecExpression extends CommonTest {

    @DisplayName("field reference")
    @Test
    public void test1() {
        context.push(typeInfo);

        String encoded = "[\"F\",[\"Ta.b.C\",\"Ff(0)\"]]";
        FieldReference fr = runtime.newFieldReference(f);
        assertEquals(encoded, codec.encodeVariable(context, fr).toString());
        CodecImpl.D d = makeD(encoded);
        assertEquals(fr, codec.decodeVariable(context, d));
    }

    @DisplayName("field reference with local variable scope in enclosed expression")
    @Test
    public void test2() {
        context.push(typeInfo);
        LocalVariable lv = runtime.newLocalVariable("lv", typeInfo.asParameterizedType());
        VariableExpression scope = runtime.newVariableExpression(lv);
        EnclosedExpression ee = runtime.newEnclosedExpressionBuilder().setExpression(scope).build();
        FieldReference fr = runtime.newFieldReference(f, ee, f.type());
        assertEquals("(lv).f", fr.toString());

        String encoded = "[\"F\",[\"Ta.b.C\",\"Ff(0)\"],[\"enclosedExpression\",[\"variableExpression\",[\"L\",\"lv\",\"Ta.b.C\"]]]]";
        assertEquals(encoded, codec.encodeVariable(context, fr).toString());
        CodecImpl.D d = makeD(encoded);
        assertEquals(fr, codec.decodeVariable(context, d));
    }
}
