package org.e2immu.cstapi.element;

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

        @Fluent
        Builder addImportStatement(ImportStatement importStatement);

        @Fluent
        Builder setPackageName(String packageName);

        CompilationUnit build();
    }
}
