package org.e2immu.cstapi.runtime;

import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.type.ParameterizedType;

public interface Predefined extends PredefinedWithoutParameterizedType {
    MethodInfo assignOperator(ParameterizedType parameterizedType);

    ParameterizedType booleanParameterizedType();

    ParameterizedType boxedBooleanParameterizedType();

    ParameterizedType byteParameterizedType();

    ParameterizedType charParameterizedType();

    ParameterizedType doubleParameterizedType();

    ParameterizedType floatParameterizedType();

    AnnotationExpression functionalInterfaceAnnotationExpression();

    ParameterizedType intParameterizedType();

    int isAssignableFromTo(ParameterizedType from, ParameterizedType to, boolean covariant);

    default boolean isAssignableFromTo(ParameterizedType from, ParameterizedType to) {
        return isAssignableFromTo(from, to, true) != -1;
    }

    ParameterizedType longParameterizedType();

    ParameterizedType objectParameterizedType();

    int primitiveTypeOrder(ParameterizedType pt);

    int reversePrimitiveTypeOrder(ParameterizedType pt);

    ParameterizedType shortParameterizedType();

    ParameterizedType stringParameterizedType();

    ParameterizedType voidParameterizedType();

    ParameterizedType widestType(ParameterizedType t1, ParameterizedType t2);

    /*
    used by binaryOperator, the result cannot be null, so must be the unboxed version (this contrasts with
    ParameterizedType.commonType)
     */
    ParameterizedType widestTypeUnbox(ParameterizedType t1, ParameterizedType t2);

}
