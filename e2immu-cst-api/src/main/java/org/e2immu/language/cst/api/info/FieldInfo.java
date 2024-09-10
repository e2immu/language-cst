package org.e2immu.language.cst.api.info;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.CompilationUnit;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;

import java.util.List;
import java.util.Set;

public interface FieldInfo extends Info {

    boolean hasBeenInspected();

    String name();

    TypeInfo owner();

    List<FieldInfo> translate(TranslationMap translationMap);

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

    FieldInfo withOwnerVariableBuilder(TypeInfo newOwner);

    interface Builder extends Info.Builder<Builder> {

        @Fluent
        Builder addFieldModifier(FieldModifier fieldModifier);

        @Fluent
        Builder setInitializer(Expression initializer);
    }
}
