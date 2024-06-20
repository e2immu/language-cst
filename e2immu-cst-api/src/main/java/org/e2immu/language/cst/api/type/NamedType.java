package org.e2immu.language.cst.api.type;

import org.e2immu.language.cst.api.runtime.Predefined;

public interface NamedType {
    ParameterizedType asParameterizedType(Predefined runtime);

    ParameterizedType asSimpleParameterizedType();

    String simpleName();
}
