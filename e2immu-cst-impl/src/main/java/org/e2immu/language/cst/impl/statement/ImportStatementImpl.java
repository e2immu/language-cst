package org.e2immu.language.cst.impl.statement;

import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.ImportStatement;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Block;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.SpaceEnum;
import org.e2immu.language.cst.impl.output.SymbolEnum;
import org.e2immu.language.cst.impl.output.TextImpl;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ImportStatementImpl extends StatementImpl implements ImportStatement {
    private final String importString;
    private final boolean isStatic;

    public ImportStatementImpl(String importString, boolean isStatic) {
        this.importString = importString;
        this.isStatic = isStatic;
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
    public boolean isStatic() {
        return isStatic;
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

    @Override
    public boolean hasSubBlocks() {
        return false;
    }

    @Override
    public String name() {
        return "import";
    }

    @Override
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (haveDirectTranslation(direct, this)) return direct;
        return List.of(this);
    }

    @Override
    public Statement withBlocks(List<Block> tSubBlocks) {
        return this;// no blocks
    }
}
