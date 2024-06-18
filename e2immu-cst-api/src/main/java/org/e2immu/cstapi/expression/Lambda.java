package org.e2immu.cstapi.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.ParameterInfo;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.type.ParameterizedType;

import java.util.List;

public interface Lambda extends Expression {
    MethodInfo methodInfo();

    default TypeInfo abstractFunctionalTypeInfo() {
        return concreteFunctionalType().typeInfo();
    }

    default ParameterizedType concreteFunctionalType() {
        return methodInfo().typeInfo().interfacesImplemented().get(0);
    }

    default ParameterizedType parameterizedType() {
        return implementation();
    }

    default ParameterizedType implementation() {
        return methodInfo().typeInfo().asSimpleParameterizedType();
    }

    default ParameterizedType concreteReturnType() {
        return methodInfo().returnType();
    }

    List<OutputVariant> outputVariants();

    interface OutputVariant {
        boolean isEmpty();

        OutputBuilder print(ParameterInfo parameterInfo, Qualification qualification);
    }

    interface Builder extends Expression.Builder<Builder> {

        @Fluent
        Builder setMethodInfo(MethodInfo methodInfo);

        @Fluent
        Builder setOutputVariants(List<OutputVariant> outputVariants);

        Lambda build();
    }
}
