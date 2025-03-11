package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.info.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InfoMapImpl implements InfoMap {
    private final Map<String, TypeInfo> typeInfoMap = new HashMap<>();
    private final Map<String, MethodInfo> methodInfoMap = new HashMap<>();
    private final Map<String, FieldInfo> fieldInfoMap = new HashMap<>();
    private final Map<String, ParameterInfo> parameterInfoMap = new HashMap<>();

    @Override
    public void put(TypeInfo typeInfo) {
        TypeInfo previous = typeInfoMap.put(typeInfo.fullyQualifiedName(), typeInfo);
        assert previous == null : "Already put in a type for FQN " + typeInfo.fullyQualifiedName();
    }

    @Override
    public void put(MethodInfo methodInfo) {
        MethodInfo previous = methodInfoMap.put(methodInfo.fullyQualifiedName(), methodInfo);
        assert previous == null : "Already put in a method for FQN " + methodInfo.fullyQualifiedName();
    }

    @Override
    public void put(FieldInfo fieldInfo) {
        FieldInfo previous = fieldInfoMap.put(fieldInfo.fullyQualifiedName(), fieldInfo);
        assert previous == null : "Already put in a field for FQN " + fieldInfo.fullyQualifiedName();
    }

    @Override
    public void put(ParameterInfo parameterInfo) {
        ParameterInfo previous = parameterInfoMap.put(parameterInfo.fullyQualifiedName(), parameterInfo);
        assert previous == null : "Already put in a parameter for FQN " + parameterInfo.fullyQualifiedName();
    }

    @Override
    public TypeInfo typeInfo(TypeInfo typeInfo) {
        String fqn = typeInfo.fullyQualifiedName();
        return Objects.requireNonNull(typeInfoMap.get(fqn), "Should have been present: " + fqn);
    }

    @Override
    public TypeInfo typeInfoNullIfAbsent(TypeInfo typeInfo) {
        return typeInfoMap.get(typeInfo.fullyQualifiedName());
    }

    @Override
    public TypeInfo typeInfoRecurse(TypeInfo typeInfo) {
        String fqn = typeInfo.fullyQualifiedName();
        TypeInfo inMap = typeInfoMap.get(fqn);
        if (inMap == null) {
            TypeInfo rewired = typeInfo.rewirePhase1(this);
            assert rewired != null : "Rewiring of " + fqn + " returns null";
            assert typeInfoMap.containsKey(fqn);
            return rewired;
        }
        return inMap;
    }

    @Override
    public MethodInfo methodInfo(MethodInfo methodInfo) {
        return Objects.requireNonNull(methodInfoMap.get(methodInfo.fullyQualifiedName()));
    }

    @Override
    public FieldInfo fieldInfo(FieldInfo fieldInfo) {
        return Objects.requireNonNull(fieldInfoMap.get(fieldInfo.fullyQualifiedName()));
    }

    @Override
    public ParameterInfo parameterInfo(ParameterInfo parameterInfo) {
        return Objects.requireNonNull(parameterInfoMap.get(parameterInfo.fullyQualifiedName()));
    }
}
