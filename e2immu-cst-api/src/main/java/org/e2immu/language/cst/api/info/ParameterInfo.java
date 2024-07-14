package org.e2immu.language.cst.api.info;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.variable.Variable;

public interface ParameterInfo extends Variable, Info {
    int index();

    String name();

    boolean isVarArgs();

    boolean isFinal();

    Builder builder();

    interface Builder extends Info.Builder<Builder> {

        @Fluent
        Builder setIsFinal(boolean isFinal);

        @Fluent
        Builder setVarArgs(boolean varArgs);
    }

    // from analysis

    boolean isModified();

    boolean isIgnoreModifications();

    Value.AssignedToField assignedToField();

    MethodInfo methodInfo();
}
