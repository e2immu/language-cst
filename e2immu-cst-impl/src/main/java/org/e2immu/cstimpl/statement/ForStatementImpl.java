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
import org.e2immu.cstapi.statement.ForStatement;
import org.e2immu.cstapi.statement.LocalVariableCreation;
import org.e2immu.cstapi.statement.Statement;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.KeywordImpl;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForStatementImpl extends StatementImpl implements ForStatement {
    private final List<Element> initializers;
    private final List<Expression> updaters;
    private final Expression expression;
    private final Block block;

    public ForStatementImpl(List<Comment> comments, Source source, List<AnnotationExpression> annotations,
                            String label, List<Element> initializers, Expression expression,
                            List<Expression> updaters, Block block) {
        super(comments, source, annotations, 1 + expression.complexity()
                                             + initializers.stream().mapToInt(Element::complexity).sum()
                                             + updaters.stream().mapToInt(Expression::complexity).sum(), label);
        this.block = block;
        this.initializers = initializers;
        this.updaters = updaters;
        this.expression = expression;
    }

    public static class Builder extends StatementImpl.Builder<ForStatement.Builder> implements ForStatement.Builder {
        private final List<Element> initializers = new ArrayList<>();
        private final List<Expression> updaters = new ArrayList<>();
        private Expression expression;
        private Block block;

        @Override
        public ForStatement.Builder addInitializer(Element initializer) {
            initializers.add(initializer);
            return this;
        }

        @Override
        public ForStatement.Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public ForStatement.Builder addUpdater(Expression updater) {
            updaters.add(updater);
            return this;
        }

        @Override
        public ForStatement.Builder setBlock(Block block) {
            this.block = block;
            return this;
        }

        @Override
        public ForStatement build() {
            return new ForStatementImpl(comments, source, annotations, label, List.copyOf(initializers),
                    expression, List.copyOf(updaters), block);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForStatementImpl that)) return false;
        return Objects.equals(initializers, that.initializers) && Objects.equals(updaters, that.updaters)
               && Objects.equals(expression, that.expression) && Objects.equals(block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initializers, updaters, expression, block);
    }

    @Override
    public List<Element> initializers() {
        return initializers;
    }

    @Override
    public List<Expression> updaters() {
        return updaters;
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
            initializers.forEach(e -> e.visit(predicate));
            expression.visit(predicate);
            updaters.forEach(e -> e.visit(predicate));
            block.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            initializers.forEach(e -> e.visit(visitor));
            expression.visit(visitor);
            updaters.forEach(e -> e.visit(visitor));
            visitor.startSubBlock(0);
            block.visit(visitor);
            visitor.endSubBlock(0);
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification);
        return outputBuilder.add(KeywordImpl.FOR)
                .add(SymbolEnum.LEFT_PARENTHESIS)
                .add(initializers.stream().map(expression1 -> expression1.print(qualification))
                        .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)))
                .add(SymbolEnum.SEMICOLON)
                .add(expression.print(qualification))
                .add(SymbolEnum.SEMICOLON)
                .add(updaters.stream().map(expression2 -> expression2.print(qualification))
                        .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)))
                .add(SymbolEnum.RIGHT_PARENTHESIS)
                .add(block.print(qualification));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(
                Stream.concat(initializers.stream().flatMap(e -> e.variables(descendMode)),
                        expression.variables(descendMode)),
                Stream.concat(updaters.stream().flatMap(e -> e.variables(descendMode)),
                        block.variables(descendMode)));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.concat(
                Stream.concat(initializers.stream().flatMap(Element::typesReferenced),
                        expression.typesReferenced()),
                Stream.concat(updaters.stream().flatMap(Expression::typesReferenced),
                        block.typesReferenced()));
    }

    @Override
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (haveDirectTranslation(direct, this)) return direct;

        // translations in order of appearance
        List<Element> initializers = initializers().stream()
                .map(init -> init instanceof LocalVariableCreation lvc
                        ? lvc.translate(translationMap).get(0)
                        : init instanceof Expression e
                        ? e.translate(translationMap) : null)
                .collect(Collectors.toList());
        Expression tex = expression.translate(translationMap);
        List<Expression> updaters = updaters().stream()
                .map(updater -> updater.translate(translationMap)).
                collect(Collectors.toList());
        List<Statement> translatedBlock = block().translate(translationMap);

        return List.of(new ForStatementImpl(comments(), source(), annotations(), label(), initializers,
                tex, updaters, ensureBlock(translatedBlock)));
    }
}
