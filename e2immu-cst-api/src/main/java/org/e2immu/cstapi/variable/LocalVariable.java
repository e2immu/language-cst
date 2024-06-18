package org.e2immu.cstapi.variable;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.type.ParameterizedType;

/*
value held by the local variable at the time of creation.
can be null (not known, not relevant)
 */
public interface LocalVariable extends Variable {

    Expression assignmentExpression();

    @Override
    default String fullyQualifiedName() {
        return simpleName();
    }

    LocalVariable withType(ParameterizedType type);

    /**
     * @param name the new name
     * @return a new local variable object, with the new name
     */
    LocalVariable withName(String name);

    /**
     * @param expression the new assignment expression
     * @return a new local variable object, with the new assignment expression
     */
    LocalVariable withAssignmentExpression(Expression expression);
}
