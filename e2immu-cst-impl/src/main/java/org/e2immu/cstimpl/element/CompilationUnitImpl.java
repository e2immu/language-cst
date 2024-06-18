package org.e2immu.cstimpl.element;

import org.e2immu.cstapi.element.*;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CompilationUnitImpl extends ElementImpl implements CompilationUnit {
    private final URI uri;
    private final String packageName;
    private final List<ImportStatement> importStatements;
    private final List<Comment> comments;

    public CompilationUnitImpl(URI uri,
                               List<Comment> comments,
                               String packageName,
                               List<ImportStatement> importStatements) {
        this.uri = uri;
        this.packageName = packageName;
        this.importStatements = importStatements;
        this.comments = comments;
    }

    @Override
    public URI uri() {
        return uri;
    }

    @Override
    public String packageName() {
        return packageName;
    }

    @Override
    public List<ImportStatement> importStatements() {
        return importStatements;
    }

    @Override
    public int complexity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Comment> comments() {
        return comments;
    }

    @Override
    public Source source() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        importStatements.forEach(predicate::test);
    }

    @Override
    public void visit(Visitor visitor) {
        importStatements.forEach(is -> is.visit(visitor));
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        throw new UnsupportedOperationException();
    }

    public static class Builder extends ElementImpl.Builder<CompilationUnit.Builder> implements CompilationUnit.Builder {
        private String packageName;
        private URI uri;
        private final List<ImportStatement> importStatements = new ArrayList<>();

        @Override
        public CompilationUnit.Builder setURI(URI uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public CompilationUnit.Builder addImportStatement(ImportStatement importStatement) {
            this.importStatements.add(importStatement);
            return this;
        }

        @Override
        public CompilationUnit.Builder setPackageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        @Override
        public CompilationUnit build() {
            return new CompilationUnitImpl(uri, comments, packageName, List.copyOf(importStatements));
        }
    }
}
