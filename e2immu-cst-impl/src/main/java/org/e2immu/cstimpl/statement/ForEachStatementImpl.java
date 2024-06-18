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
import org.e2immu.cstapi.statement.ForEachStatement;
import org.e2immu.cstapi.statement.LocalVariableCreation;
import org.e2immu.cstapi.statement.Statement;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.LocalVariable;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.*;
import org.e2immu.cstimpl.type.DiamondEnum;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ForEachStatementImpl extends StatementImpl implements ForEachStatement {
    private final LocalVariableCreation initializer;
    private final Expression expression;
    private final Block block;

    public ForEachStatementImpl(List<Comment> comments,
                                Source source,
                                List<AnnotationExpression> annotations,
                                String label,
                                LocalVariableCreation initializer,
                                Expression expression, Block block) {
        super(comments, source, annotations, 0, label);
        this.initializer = initializer;
        this.expression = expression;
        this.block = block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForEachStatementImpl that)) return false;
        return Objects.equals(initializer, that.initializer) && Objects.equals(expression, that.expression)
               && Objects.equals(block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initializer, expression, block);
    }

    public static class Builder extends StatementImpl.Builder<ForEachStatement.Builder> implements ForEachStatement.Builder {
        private LocalVariableCreation initializer;
        private Expression expression;
        private Block block;

        @Override
        public ForEachStatement.Builder setInitializer(LocalVariableCreation initializer) {
            this.initializer = initializer;
            return this;
        }

        @Override
        public ForEachStatement.Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public ForEachStatement.Builder setBlock(Block block) {
            this.block = block;
            return this;
        }

        @Override
        public ForEachStatement build() {
            return new ForEachStatementImpl(comments, source, annotations, label, initializer, expression, block);
        }
    }

    @Override
    public LocalVariableCreation initializer() {
        return initializer;
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
            initializer.visit(predicate);
            expression.visit(predicate);
            block.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            initializer.visit(visitor);
            expression.visit(visitor);
            visitor.startSubBlock(0);
            block.visit(visitor);
            visitor.endSubBlock(0);
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification);
        LocalVariable lv = initializer.localVariable();
        return outputBuilder.add(KeywordImpl.FOR)
                .add(SymbolEnum.LEFT_PARENTHESIS)
                .add(initializer.isVar() ? new OutputBuilderImpl().add(KeywordImpl.VAR)
                        : lv.parameterizedType().print(qualification, false, DiamondEnum.SHOW_ALL))
                .add(SpaceEnum.ONE)
                .add(new TextImpl(lv.simpleName()))
                .add(SymbolEnum.COLON)
                .add(expression.print(qualification))
                .add(SymbolEnum.RIGHT_PARENTHESIS)
                .add(block.print(qualification));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(expression.variables(descendMode), block.variables(descendMode));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.concat(initializer.typesReferenced(),
                Stream.concat(expression.typesReferenced(), block.typesReferenced()));
    }

    @Override
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (haveDirectTranslation(direct, this)) return direct;

        // translations in order of appearance
        LocalVariableCreation translatedLvc = (LocalVariableCreation) initializer.translate(translationMap).get(0);
        Expression translated = expression.translate(translationMap);
        List<Statement> translatedBlock = block.translate(translationMap);
        if (translatedLvc == initializer && expression == translated && translatedBlock.get(0) == block) {
            return List.of(this);
        }
        return List.of(new ForEachStatementImpl(comments(), source(), annotations(), label(), translatedLvc, translated,
                ensureBlock(translatedBlock)));
    }
}
