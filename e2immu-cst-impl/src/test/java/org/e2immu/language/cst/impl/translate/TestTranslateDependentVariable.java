package org.e2immu.language.cst.impl.translate;

import org.e2immu.language.cst.api.info.FieldInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.*;
import org.e2immu.language.cst.impl.runtime.RuntimeImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTranslateDependentVariable {
    private final Runtime r = new RuntimeImpl();

    @Test
    public void test() {
        TypeInfo ti = r.newTypeInfo(r.stringTypeInfo(), "X");
        FieldInfo fi = r.newFieldInfo("array", false, r.intParameterizedType().copyWithArrays(1), ti);
        LocalVariable x = r.newLocalVariable("x", ti.asParameterizedType());
        FieldReference fr = r.newFieldReference(fi, r.newVariableExpression(x), fi.type());
        assertEquals("x.array", fr.toString());
        LocalVariable i = r.newLocalVariable("i", r.intParameterizedType());
        DependentVariable dv = r.newDependentVariable(r.newVariableExpression(fr), r.newVariableExpression(i));
        assertEquals("x.array[i]", dv.toString());

        This thisVar = r.newThis(ti.asParameterizedType());
        TranslationMap tm = r.newTranslationMapBuilder()
                .put(x, thisVar) // variable
                .put(r.newVariableExpression(i), r.intOne()) // expression
                .build();
        Variable variable = tm.translateVariable(dv);
        assertEquals("this.array[1]", variable.toString());
    }
}
