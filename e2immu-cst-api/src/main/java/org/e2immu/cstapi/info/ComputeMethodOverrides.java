package org.e2immu.cstapi.info;

import org.e2immu.cstapi.type.NamedType;
import org.e2immu.cstapi.type.ParameterizedType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ComputeMethodOverrides {
    /*
    uses the same machinery as 'overrides', but is executed at a different stage.
     */
    MethodInfo computeFunctionalInterface(TypeInfo typeInfo);

    Set<MethodInfo> overrides(MethodInfo methodInfo);

    Map<NamedType, ParameterizedType> mapOfSuperType(ParameterizedType superType);

    boolean sameParameters(List<ParameterInfo> parametersOfMyMethod,
                           List<ParameterInfo> parametersOfTarget,
                           Map<NamedType, ParameterizedType> translationMap);

    List<ParameterizedType> directSuperTypes(TypeInfo typeInfo);
}
