package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.TextImpl;
import org.e2immu.support.SetOnce;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CompilationUnitImpl extends ElementImpl implements CompilationUnit {
    private final URI uri;
    private final String packageName;
    private final SetOnce<List<ImportStatement>> importStatements = new SetOnce<>();
    private final List<Comment> comments;

    public CompilationUnitImpl(URI uri,
                               List<Comment> comments,
                               String packageName) {
        this.uri = uri;
        this.packageName = packageName;
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
        return importStatements.get();
    }

    @Override
    public void setImportStatements(List<ImportStatement> importStatements) {
        this.importStatements.set(importStatements);
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
        importStatements.get().forEach(predicate::test);
    }

    @Override
    public void visit(Visitor visitor) {
        importStatements.get().forEach(is -> is.visit(visitor));
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder ob = new OutputBuilderImpl();
        if (packageName != null && !packageName.isBlank()) ob.add(new TextImpl(packageName));
        if (uri != null) ob.add(new TextImpl(" [" + uri + "]"));
        return ob;
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

        @Override
        public CompilationUnit.Builder setURI(URI uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public CompilationUnit.Builder setURIString(String s) {
            try {
                this.uri = new URI(s);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return this;
        }


        @Override
        public CompilationUnit.Builder setPackageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        @Override
        public CompilationUnit build() {
            return new CompilationUnitImpl(uri, comments, packageName);
        }
    }
}
