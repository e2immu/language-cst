package org.e2immu.language.cst.api.info;

public interface InfoMap {
    // can only be done once for each typeInfo object
    void put(TypeInfo typeInfo);

    void put(MethodInfo methodInfo);

    void put(FieldInfo fieldInfo);

    void put(ParameterInfo parameterInfo);


    // do not recurse, error if absent
    TypeInfo typeInfo(TypeInfo typeInfo);

    TypeInfo typeInfoRecurse(TypeInfo typeInfo);

    TypeInfo typeInfoNullIfAbsent(TypeInfo typeInfo);

    MethodInfo methodInfo(MethodInfo methodInfo);

    FieldInfo fieldInfo(FieldInfo fieldInfo);

    ParameterInfo parameterInfo(ParameterInfo parameterInfo);
}
