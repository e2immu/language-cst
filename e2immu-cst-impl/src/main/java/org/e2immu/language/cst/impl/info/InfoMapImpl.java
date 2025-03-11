package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.info.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InfoMapImpl implements InfoMap {
    private final Map<String, TypeInfo> typeInfoMap = new HashMap<>();

    @Override
    public void put(TypeInfo typeInfo) {
        TypeInfo previous = typeInfoMap.put(typeInfo.fullyQualifiedName(), typeInfo);
        assert previous == null : "Already put an object in for FQN " + typeInfo.fullyQualifiedName();
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
        return null;
    }

    @Override
    public FieldInfo fieldInfo(FieldInfo fieldInfo) {
        return null;
    }

    @Override
    public ParameterInfo parameterInfo(ParameterInfo parameterInfo) {
        return null;
    }
}
