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
            if (type.isPrimitiveExcludingVoid()) {
                return runtime.newBoolean(testType.equals(type.ensureBoxed(runtime)));
            }
            if (testType.equals(type)) {
                return runtime.constantTrue();
            }
            if (!testType.isAssignableFrom(runtime, type)) {
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
