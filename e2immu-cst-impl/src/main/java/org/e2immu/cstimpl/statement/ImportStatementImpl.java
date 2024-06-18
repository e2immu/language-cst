package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.ImportStatement;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.SpaceEnum;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.output.TextImpl;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ImportStatementImpl extends StatementImpl implements ImportStatement {
    private final String importString;

    public ImportStatementImpl(String importString) {
        this.importString = importString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImportStatementImpl that)) return false;
        return Objects.equals(importString, that.importString);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(importString);
    }

    @Override
    public String importString() {
        return importString;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        predicate.test(this);
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.beforeStatement(this);
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return outputBuilder(qualification).add(new TextImpl("import")).add(SpaceEnum.ONE)
                .add(new TextImpl(importString)).add(SymbolEnum.SEMICOLON).add(SpaceEnum.NEWLINE);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.empty();
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.empty();
    }
}
