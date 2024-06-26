package org.e2immu.language.cst.api.element;

import org.e2immu.annotation.Fluent;

import java.net.URI;
import java.util.List;

public interface CompilationUnit extends Element {

    URI uri();

    String packageName();

    List<ImportStatement> importStatements();

    interface Builder extends Element.Builder<Builder> {

        @Fluent
        Builder setURI(URI uri);

        // to avoid having to catch exceptions in PredefinedImpl
        @Fluent
        Builder setURIString(String s);

        @Fluent
        Builder addImportStatement(ImportStatement importStatement);

        @Fluent
        Builder setPackageName(String packageName);

        CompilationUnit build();
    }
}
