package org.e2immu.language.cst.api.variable;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.FieldInfo;

public interface FieldReference extends Variable {
    FieldInfo fieldInfo();

    /**
     * when null, the scope is implicitly an instance of "this"
     *
     * @return the scope of the field, as in "scope.field" or "someMethod().field"
     */
    Expression scope();

    /**
     * @return not-null when the <code>scope()</code> expression is a variable
     */
    Variable scopeVariable();

    boolean scopeIsRecursivelyThis();

    boolean isDefaultScope();

    boolean scopeIsThis();

    @Override
    default FieldReference fieldReferenceScope() {
        return this;
    }

    @Override
    default Variable fieldReferenceBase() {
        Variable scopeVariable = scopeVariable();
        if (scopeVariable instanceof FieldReference fr) return fr.fieldReferenceBase();
        return scopeVariable;
    }
}
