package org.e2immu.cstapi.info;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.element.CompilationUnit;
import org.e2immu.cstapi.element.Element;

public interface Info extends Element {
    Access access();

    CompilationUnit compilationUnit();

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
