package org.e2immu.language.cst.impl.expression.eval;

import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.runtime.Runtime;

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
        if (instanceOf.testType().isJavaLangObject()) {
            return runtime.constantTrue();
        }
        VariableExpression ve;
        if ((ve = value.asInstanceOf(VariableExpression.class)) != null) {
            if (instanceOf.testType().isAssignableFrom(runtime, ve.variable().parameterizedType())) {
                return runtime.constantTrue();
            }
            if (instanceOf.patternVariable() == null) {
                return runtime.constantFalse();
            }
            // keep as is
            return instanceOf;
        }
        Instance instance;
        if ((instance = value.asInstanceOf(Instance.class)) != null) {
            boolean isAssignable = instanceOf.testType().isAssignableFrom(runtime, instance.parameterizedType());
            return runtime.newBoolean(isAssignable);
        }
        return instanceOf;
    }
}
