package org.e2immu.language.cst.impl.shallowanalyzer;

import org.e2immu.annotation.*;
import org.e2immu.annotation.rare.IgnoreModifications;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
Convert annotations in the Info objects of the CST to analysis properties.
Mainly used for testing, when working without the language-inspection modules.
 */
public class ShallowAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShallowAnalyzer.class);

    private final Runtime runtime;

    public ShallowAnalyzer(Runtime runtime) {
        this.runtime = runtime;
    }

    public void analyze(TypeInfo typeInfo) {
        try {
            for (TypeInfo sub : typeInfo.subTypes()) {
                analyze(sub);
            }
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
            methodInfo.analysis().set(PropertyImpl.NOT_NULL_METHOD, isNotNull
                    ? ValueImpl.NotNullImpl.NOT_NULL : ValueImpl.NotNullImpl.NULLABLE);

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
