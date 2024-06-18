package org.e2immu.cstapi.variable;

import org.e2immu.cstapi.info.TypeInfo;

public interface This extends Variable {
    TypeInfo typeInfo();

    boolean writeSuper();
}
