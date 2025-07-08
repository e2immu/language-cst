package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.impl.statement.BlockImpl;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InfoMapImpl implements InfoMap {
    private final Map<String, TypeInfo> typeInfoMap = new ConcurrentHashMap<>();
    private final Map<String, MethodInfo> methodInfoMap = new ConcurrentHashMap<>();
    private final Map<String, FieldInfo> fieldInfoMap = new ConcurrentHashMap<>();
    private final Map<String, ParameterInfo> parameterInfoMap = new ConcurrentHashMap<>();
    private final Set<TypeInfo> setOfTypesToRewire;

    public InfoMapImpl(Set<TypeInfo> setOfTypesToRewire) {
        this.setOfTypesToRewire = setOfTypesToRewire;
    }

    @Override
    public void put(TypeInfo typeInfo) {
        TypeInfo previous = typeInfoMap.put(typeInfo.fullyQualifiedName(), typeInfo);
        assert previous == null : "Already put in a type for FQN " + typeInfo.fullyQualifiedName();
    }

    @Override
    public void put(String fqn, MethodInfo methodInfo) {
        MethodInfo previous = methodInfoMap.put(fqn, methodInfo);
        assert previous == null : "Already put in a method for FQN " + fqn;
    }

    @Override
    public void put(FieldInfo fieldInfo) {
        FieldInfo previous = fieldInfoMap.put(fieldInfo.fullyQualifiedName(), fieldInfo);
        assert previous == null : "Already put in a field for FQN " + fieldInfo.fullyQualifiedName();
    }

    @Override
    public void put(String fqn, ParameterInfo parameterInfo) {
        ParameterInfo previous = parameterInfoMap.put(fqn, parameterInfo);
        assert previous == null : "Already put in a parameter for FQN " + fqn;
    }

    @Override
    public TypeInfo typeInfo(TypeInfo typeInfo) {
        if (!setOfTypesToRewire.contains(typeInfo.primaryType())) return typeInfo;
        String fqn = typeInfo.fullyQualifiedName();
        return Objects.requireNonNull(typeInfoMap.get(fqn), "Should have been present: " + fqn);
    }

    @Override
    public TypeInfo typeInfoNullIfAbsent(TypeInfo typeInfo) {
        if (!setOfTypesToRewire.contains(typeInfo.primaryType())) return typeInfo;
        return typeInfoMap.get(typeInfo.fullyQualifiedName());
    }

    @Override
    public TypeInfo typeInfoRecurse(TypeInfo typeInfo) {
        if (!setOfTypesToRewire.contains(typeInfo.primaryType())) return typeInfo;

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
    public TypeInfo typeInfoRecurseAllPhases(TypeInfo typeInfo) {
        String fqn = typeInfo.fullyQualifiedName();
        TypeInfo inMap = typeInfoMap.get(fqn);
        if (inMap != null) return inMap;
        TypeInfo rewired = typeInfo.rewirePhase1(this);
        assert rewired != null : "Rewiring of " + fqn + " returns null";
        assert typeInfoMap.containsKey(fqn);
        typeInfo.rewirePhase2(this);
        typeInfo.rewirePhase3(this);
        return rewired;
    }

    @Override
    public MethodInfo methodInfo(MethodInfo methodInfo) {
        assert methodInfo != null;
        if (methodInfo.isSyntheticArrayConstructor()) {
            // the synthetic array constructor won't be in the map
            MethodInfo mi = new MethodInfoImpl(MethodInfoImpl.MethodTypeEnum.SYNTHETIC_ARRAY_CONSTRUCTOR,
                    "<init>", typeInfo(methodInfo.typeInfo()));
            mi.builder()
                    .addAnnotations(methodInfo.annotations().stream()
                            .map(a -> (AnnotationExpression) a.rewire(this)).toList())
                    .addComments(methodInfo.comments().stream().map(c -> c.rewire(this)).toList())
                    .setSource(methodInfo.source())
                    .setReturnType(methodInfo.returnType().rewire(this))
                    .addMethodModifier(MethodModifierEnum.PUBLIC)
                    .setMethodBody(new BlockImpl.Builder().build())
                    .setMissingData(methodInfo.missingData())
                    .computeAccess();
            for (int i = 0; i < methodInfo.returnType().arrays(); i++) {
                ParameterInfo pii = methodInfo.parameters().get(i);
                ParameterInfo pi = mi.builder().addParameter(pii.name(), pii.parameterizedType());
                pi.builder()
                        .addAnnotations(methodInfo.annotations().stream()
                                .map(a -> (AnnotationExpression) a.rewire(this)).toList())
                        .addComments(pii.comments().stream().map(c -> c.rewire(this)).toList())
                        .setSource(pii.source());
            }
            mi.builder().commitParameters().commit();
            // and we don't store it either
            return mi;
        }
        if (!setOfTypesToRewire.contains(methodInfo.typeInfo().primaryType())) return methodInfo;
        MethodInfo rewired = methodInfoMap.get(methodInfo.fullyQualifiedName());
        assert rewired != null;
        return rewired;
    }

    @Override
    public FieldInfo fieldInfo(FieldInfo fieldInfo) {
        if (!setOfTypesToRewire.contains(fieldInfo.owner().primaryType())) return fieldInfo;
        return Objects.requireNonNull(fieldInfoMap.get(fieldInfo.fullyQualifiedName()));
    }

    @Override
    public ParameterInfo parameterInfo(ParameterInfo parameterInfo) {
        if (!setOfTypesToRewire.contains(parameterInfo.typeInfo().primaryType())) return parameterInfo;
        return Objects.requireNonNull(parameterInfoMap.get(parameterInfo.fullyQualifiedName()));
    }
}
