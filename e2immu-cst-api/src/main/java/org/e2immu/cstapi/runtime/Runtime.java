package org.e2immu.cstapi.runtime;

import org.e2immu.cstapi.info.ComputeMethodOverrides;

public interface Runtime extends Predefined, Factory, Eval, Types {

    Configuration configuration();

    ComputeMethodOverrides computeMethodOverrides();
}
