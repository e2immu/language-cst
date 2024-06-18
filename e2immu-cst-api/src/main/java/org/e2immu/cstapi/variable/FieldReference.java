package org.e2immu.cstapi.variable;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.info.FieldInfo;

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
}
