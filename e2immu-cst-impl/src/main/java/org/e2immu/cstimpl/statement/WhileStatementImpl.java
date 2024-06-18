package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.Block;
import org.e2immu.cstapi.statement.Statement;
import org.e2immu.cstapi.statement.WhileStatement;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.KeywordImpl;
import org.e2immu.cstimpl.output.SymbolEnum;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WhileStatementImpl extends StatementImpl implements WhileStatement {
    private final Expression expression;
    private final Block block;

    public WhileStatementImpl(List<Comment> comments,
                              Source source,
                              List<AnnotationExpression> annotations,
                              String label,
                              Expression expression, Block block) {
        super(comments, source, annotations, 0, label);
        this.expression = expression;
        this.block = block;
    }

    public static class Builder extends StatementImpl.Builder<WhileStatement.Builder> implements WhileStatement.Builder {
        private Expression expression;
        private Block block;

        @Override
        public WhileStatement.Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public WhileStatement.Builder setBlock(Block block) {
            this.block = block;
            return this;
        }

        @Override
        public WhileStatement build() {
            return new WhileStatementImpl(comments, source, annotations, label, expression, block);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhileStatementImpl that)) return false;
        return Objects.equals(expression, that.expression) && Objects.equals(block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, block);
    }

    @Override
    public Expression expression() {
        return expression;
    }

    @Override
    public Block block() {
        return block;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            expression.visit(predicate);
            block.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            expression.visit(visitor);
            visitor.startSubBlock(0);
            block.visit(visitor);
            visitor.endSubBlock(0);
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return outputBuilder(qualification)
                .add(KeywordImpl.WHILE)
                .add(SymbolEnum.LEFT_PARENTHESIS)
                .add(expression().print(qualification))
                .add(SymbolEnum.RIGHT_PARENTHESIS)
                .add(block.print(qualification));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(expression.variables(descendMode), block.variables(descendMode));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.concat(expression.typesReferenced(), block.typesReferenced());
    }

    @Override
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (haveDirectTranslation(direct, this)) return direct;

        Expression tex = expression.translate(translationMap);
        List<Statement> translatedBlock = block.translate(translationMap);
        if (tex == expression && !haveDirectTranslation(translatedBlock, block)) return List.of(this);
        WhileStatement newWhile = new WhileStatementImpl(comments(), source(), annotations(), label(), tex,
                ensureBlock(translatedBlock));
        return List.of(newWhile);
    }
}
