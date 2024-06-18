package org.e2immu.cstapi.output;

import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.variable.Variable;

public interface Qualification {
    boolean doNotQualifyImplicit();

    boolean isFullyQualifiedNames();

    TypeNameRequired qualifierRequired(TypeInfo typeInfo);

    boolean qualifierRequired(MethodInfo methodInfo);

    boolean qualifierRequired(Variable variable);
}
