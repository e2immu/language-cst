package org.e2immu.cstapi.info;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.analysis.Value;
import org.e2immu.cstapi.variable.Variable;

public interface ParameterInfo extends Variable {
    int index();

    String name();

    boolean isVarArgs();

    Builder builder();

    interface Builder extends Info.Builder<Builder> {

        @Fluent
        Builder setVarArgs(boolean varArgs);
    }

    // from analysis

    boolean isModified();

    boolean isIgnoreModifications();

    Value.AssignedToField assignedToField();
}
