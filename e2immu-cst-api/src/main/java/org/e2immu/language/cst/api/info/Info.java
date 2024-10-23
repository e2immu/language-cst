package org.e2immu.language.cst.api.info;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.CompilationUnit;
import org.e2immu.language.cst.api.element.Element;

public interface Info extends Element {

    String info();

    Access access();

    CompilationUnit compilationUnit();

    String simpleName();

    String fullyQualifiedName();

    boolean isSynthetic();

    TypeInfo typeInfo();

    interface Builder<B extends Builder<?>> extends Element.Builder<B> {
        @Fluent
        B setAccess(Access access);

        @Fluent
        B setSynthetic(boolean synthetic);

        boolean hasBeenCommitted();

        // once all the modifiers have been set
        @Fluent
        Builder computeAccess();

        void commit();
    }

}
