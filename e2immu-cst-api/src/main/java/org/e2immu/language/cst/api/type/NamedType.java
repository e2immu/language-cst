package org.e2immu.language.cst.api.type;

import org.e2immu.language.cst.api.runtime.Runtime;

public interface NamedType {
    ParameterizedType asParameterizedType(Runtime runtime);

    ParameterizedType asSimpleParameterizedType();

    String simpleName();
}
