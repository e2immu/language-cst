package org.e2immu.language.cst.api.runtime;

import org.e2immu.language.cst.api.info.ComputeMethodOverrides;

public interface Runtime extends Predefined, Factory, Eval, Types {

    LanguageConfiguration configuration();

    ComputeMethodOverrides computeMethodOverrides();
}
