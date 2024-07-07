package org.e2immu.language.cst.api.output;

import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.variable.Variable;

public interface Qualification {
    boolean doNotQualifyImplicit();

    boolean isFullyQualifiedNames();

    boolean isSimpleOnly();

    TypeNameRequired qualifierRequired(TypeInfo typeInfo);

    boolean qualifierRequired(MethodInfo methodInfo);

    boolean qualifierRequired(Variable variable);
}
