package org.e2immu.language.cst.impl.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Block;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.GuideImpl;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SpaceEnum;
import org.e2immu.language.cst.impl.output.SymbolEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlockImpl extends StatementImpl implements Block {
    private final List<Statement> statements;

    public BlockImpl() {
        this.statements = List.of();
    }

    public BlockImpl(List<Comment> comments,
                     Source source,
                     List<AnnotationExpression> annotations,
                     String label,
                     List<Statement> statements) {
        super(comments, source, annotations,
                1 + statements.stream().mapToInt(Statement::complexity).sum(), label);
        this.statements = statements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockImpl block = (BlockImpl) o;
        return Objects.equals(statements, block.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(statements);
    }

    @Override
    public List<Statement> statements() {
        return statements;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            statements.forEach(statement -> statement.visit(predicate));
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            statements.forEach(statement -> statement.visit(visitor));
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification);
        outputBuilder.add(SymbolEnum.LEFT_BRACE);
        if (!statements.isEmpty()) {
            outputBuilder.add(statements.stream()
                    .filter(s -> !s.isSynthetic())
                    .map(s -> s.print(qualification))
                    .collect(OutputBuilderImpl.joining(SpaceEnum.NONE, GuideImpl.generatorForBlock())));
        }
        outputBuilder.add(SymbolEnum.RIGHT_BRACE);
        return outputBuilder;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return statements.stream().flatMap(s -> s.variables(descendMode));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return statements.stream().flatMap(Statement::typesReferenced);
    }

    public static class Builder extends StatementImpl.Builder<Block.Builder> implements Block.Builder {
        private final List<Statement> statements = new ArrayList<>();

        @Override
        @Fluent
        public Builder addStatement(Statement statement) {
            assert statement != null;
            statements.add(statement);
            return this;
        }

        @Override
        @Fluent
        public Builder addStatements(List<Statement> statements) {
            this.statements.addAll(statements);
            return this;
        }

        @Override
        public Block build() {
            return new BlockImpl(comments, source, annotations, label, List.copyOf(statements));
        }
    }

    @Override
    public boolean hasSubBlocks() {
        return false;
    }
}
