package org.e2immu.cstapi.info;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.element.CompilationUnit;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.type.ParameterizedType;

import java.util.Set;

public interface FieldInfo extends Info {

    String name();

    TypeInfo owner();

    ParameterizedType type();

    String fullyQualifiedName();

    // inspection

    boolean isStatic();

    boolean isFinal();

    boolean isTransient();

    boolean isVolatile();


    // analysis

    boolean isPropertyNotNull();

    // as opposed to isFinal, which is the modifier
    boolean isPropertyFinal();

    boolean isIgnoreModifications();

    default CompilationUnit compilationUnit() {
        return owner().compilationUnit();
    }

    Builder builder();

    Set<FieldModifier> modifiers();

    Expression initializer();

    interface Builder extends Info.Builder<Builder> {

        @Fluent
        Builder addFieldModifier(FieldModifier fieldModifier);

        @Fluent
        Builder setInitializer(Expression initializer);
    }
}
