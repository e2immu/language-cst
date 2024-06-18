package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.variable.LocalVariable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface LocalVariableCreation extends Statement {

    interface Modifier {
        boolean isFinal(); // the only one in Java; in Kotlin, this represents "val"

        boolean isWithoutTypeSpecification(); // Java "var"
    }

    boolean isVar();

    Set<Modifier> modifiers();

    LocalVariable localVariable();

    List<LocalVariable> otherLocalVariables();

    Stream<LocalVariable> localVariableStream();

    /**
     * Convenience method
     *
     * @return all the new local variables created here
     */
    default Set<LocalVariable> newLocalVariables() {
        return localVariableStream().collect(Collectors.toUnmodifiableSet());
    }

    default boolean hasSingleDeclaration() {
        return otherLocalVariables().isEmpty();
    }

    interface Builder extends Statement.Builder<Builder> {
        @Fluent
        Builder addModifier(Modifier modifier);

        @Fluent
        Builder setLocalVariable(LocalVariable localVariable);

        @Fluent
        Builder addOtherLocalVariable(LocalVariable localVariable);

        LocalVariableCreation build();
    }
}
