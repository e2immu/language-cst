package org.e2immu.language.cst.api.element;

import org.e2immu.annotation.Fluent;

public interface ImportStatement extends Element {

    String importString();

    interface Builder extends Element.Builder<Builder> {

        @Fluent
        Builder setImport(String importString);

        ImportStatement build();
    }
}
