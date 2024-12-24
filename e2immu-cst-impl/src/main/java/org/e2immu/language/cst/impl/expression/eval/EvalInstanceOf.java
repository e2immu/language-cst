package org.e2immu.language.cst.impl.expression.eval;

import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.type.ParameterizedType;

public class EvalInstanceOf {
    private final Runtime runtime;

    public EvalInstanceOf(Runtime runtime) {
        this.runtime = runtime;
    }

    public Expression eval(InstanceOf instanceOf) {
        Expression value = instanceOf.expression();
        if (value.isNullConstant()) {
            return runtime.constantFalse();
        }
        ParameterizedType testType = instanceOf.testType();
        if (testType.isJavaLangObject()) {
            return runtime.constantTrue();
        }
        VariableExpression ve;
        if ((ve = value.asInstanceOf(VariableExpression.class)) != null) {
            ParameterizedType type = ve.variable().parameterizedType();
            // trivial cast to same or higher type
            if (testType.isAssignableFrom(runtime, type)) {
                return runtime.constantTrue();
            }
            // see TestOperators for examples
            if (!testType.typeInfo().isInterface() && !type.isAssignableFrom(runtime, testType)) {
                return runtime.constantFalse();
            }
            // keep as is
            return instanceOf;
        }
        Instance instance;
        if ((instance = value.asInstanceOf(Instance.class)) != null) {
            boolean isAssignable = testType.isAssignableFrom(runtime, instance.parameterizedType());
            return runtime.newBoolean(isAssignable);
        }
        return instanceOf;
    }
}
