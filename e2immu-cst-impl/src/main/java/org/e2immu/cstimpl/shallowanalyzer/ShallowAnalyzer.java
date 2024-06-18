package org.e2immu.cstimpl.shallowanalyzer;

import org.e2immu.annotation.*;
import org.e2immu.annotation.rare.IgnoreModifications;
import org.e2immu.cstapi.info.*;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstimpl.analysis.PropertyImpl;
import org.e2immu.cstimpl.analysis.ValueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShallowAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShallowAnalyzer.class);

    private final Runtime runtime;

    public ShallowAnalyzer(Runtime runtime) {
        this.runtime = runtime;
    }

    public void analyze(TypeInfo typeInfo) {
        try {
            for (MethodInfo methodInfo : typeInfo.methods()) {
                analyze(methodInfo);
            }
            for (FieldInfo fieldInfo : typeInfo.fields()) {
                analyze(fieldInfo);
            }
        } catch (Throwable t) {
            LOGGER.error("Caught exception in shallow analyzer for type {}", typeInfo);
            throw t;
        } finally {
            typeInfo.analysis().set(PropertyImpl.SHALLOW_ANALYZER, ValueImpl.BoolImpl.TRUE);
        }
    }

    private static boolean haveActiveAnnotation(Class<?> clazz, Info info) {
        String fqn = clazz.getCanonicalName();
        return info.annotations().stream().anyMatch(ae -> ae.typeInfo().fullyQualifiedName().equals(fqn));
    }

    private static boolean haveActiveAnnotation(Class<?> clazz, ParameterInfo info) {
        String fqn = clazz.getCanonicalName();
        return info.annotations().stream().anyMatch(ae -> ae.typeInfo().fullyQualifiedName().equals(fqn));
    }

    public void analyze(MethodInfo methodInfo) {
        try {
            boolean isFluent = haveActiveAnnotation(Fluent.class, methodInfo);
            methodInfo.analysis().set(PropertyImpl.FLUENT_METHOD, ValueImpl.BoolImpl.from(isFluent));
            boolean isIdentity = haveActiveAnnotation(Identity.class, methodInfo);
            methodInfo.analysis().set(PropertyImpl.IDENTITY_METHOD, ValueImpl.BoolImpl.from(isIdentity));
            boolean isModified = haveActiveAnnotation(Modified.class, methodInfo);
            methodInfo.analysis().set(PropertyImpl.MODIFIED_METHOD, ValueImpl.BoolImpl.from(isModified));
            boolean isNotNull = haveActiveAnnotation(NotNull.class, methodInfo);
            methodInfo.analysis().set(PropertyImpl.NOT_NULL_METHOD, ValueImpl.BoolImpl.from(isNotNull));

            for (ParameterInfo parameterInfo : methodInfo.parameters()) {
                analyze(parameterInfo);
            }
        } catch (Throwable t) {
            LOGGER.error("Caught exception in shallow analyzer for method {}", methodInfo);
            throw t;
        } finally {
            methodInfo.analysis().set(PropertyImpl.SHALLOW_ANALYZER, ValueImpl.BoolImpl.TRUE);
        }
    }

    public void analyze(FieldInfo fieldInfo) {
        try {
            boolean isFinal = fieldInfo.isFinal() || haveActiveAnnotation(Final.class, fieldInfo);
            fieldInfo.analysis().set(PropertyImpl.FINAL_FIELD, ValueImpl.BoolImpl.from(isFinal));
            boolean isIgnoreMods = haveActiveAnnotation(IgnoreModifications.class, fieldInfo);
            fieldInfo.analysis().set(PropertyImpl.IGNORE_MODIFICATIONS_FIELD, ValueImpl.BoolImpl.from(isIgnoreMods));
        } catch (Throwable t) {
            LOGGER.error("Caught exception in shallow analyzer for field {}", fieldInfo);
            throw t;
        } finally {
            fieldInfo.analysis().set(PropertyImpl.SHALLOW_ANALYZER, ValueImpl.BoolImpl.TRUE);
        }
    }

    public void analyze(ParameterInfo parameterInfo) {
        try {
            boolean isIgnoreMods = haveActiveAnnotation(IgnoreModifications.class, parameterInfo);
            parameterInfo.analysis().set(PropertyImpl.IGNORE_MODIFICATIONS_PARAMETER, ValueImpl.BoolImpl.from(isIgnoreMods));
            boolean isModified = !isIgnoreMods && haveActiveAnnotation(Modified.class, parameterInfo);
            parameterInfo.analysis().set(PropertyImpl.MODIFIED_PARAMETER, ValueImpl.BoolImpl.from(isModified));
        } catch (Throwable t) {
            LOGGER.error("Caught exception in shallow analyzer for parameter {}", parameterInfo);
            throw t;
        } finally {
            parameterInfo.analysis().set(PropertyImpl.SHALLOW_ANALYZER, ValueImpl.BoolImpl.TRUE);
        }
    }
}
