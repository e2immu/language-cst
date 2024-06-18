package org.e2immu.cstimpl.expression;

import org.e2immu.language.cst.api.expression.VariableExpression;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.cstimpl.runtime.RuntimeImpl;

public abstract class CommonTest {
    protected final Runtime r = new RuntimeImpl();

    protected final VariableExpression i = r.newVariableExpression(createVariable("i"));
    protected final VariableExpression j = r.newVariableExpression(createVariable("j"));

    protected Variable createVariable(String name) {
        return r.newLocalVariable(name, r.intParameterizedType());
    }
}
