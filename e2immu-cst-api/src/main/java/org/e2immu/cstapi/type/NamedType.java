package org.e2immu.cstapi.type;

import org.e2immu.cstapi.runtime.Runtime;

public interface NamedType {
    ParameterizedType asParameterizedType(Runtime runtime);

    ParameterizedType asSimpleParameterizedType();

    String simpleName();
}
