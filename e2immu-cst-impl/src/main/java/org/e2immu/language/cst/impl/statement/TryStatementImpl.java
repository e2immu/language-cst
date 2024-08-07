package org.e2immu.language.cst.impl.statement;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Block;
import org.e2immu.language.cst.api.statement.LocalVariableCreation;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.statement.TryStatement;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.type.DiamondEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TryStatementImpl extends StatementImpl implements TryStatement {
    private final Block block;
    private final Block finallyBlock;
    private final List<CatchClause> catchClauses;
    private final List<LocalVariableCreation> resources;

    public TryStatementImpl(List<Comment> comments,
                            Source source,
                            List<AnnotationExpression> annotations,
                            String label,
                            List<LocalVariableCreation> resources,
                            Block block,
                            List<CatchClause> catchClauses,
                            Block finallyBlock) {
        super(comments, source, annotations, 1 + block.complexity() + finallyBlock.complexity()
                                             + catchClauses.stream().mapToInt(CatchClause::complexity).sum(), label);
        this.block = Objects.requireNonNull(block);
        this.finallyBlock = Objects.requireNonNull(finallyBlock);
        this.catchClauses = Objects.requireNonNull(catchClauses);
        this.resources = Objects.requireNonNull(resources);
    }

    public static class CatchClauseImpl implements CatchClause {
        private final List<ParameterizedType> exceptionTypes;
        private final String variableName;
        private final Block block;

        public CatchClauseImpl(List<ParameterizedType> exceptionTypes, String variableName, Block block) {
            this.variableName = variableName;
            this.exceptionTypes = exceptionTypes;
            this.block = block;
        }

        public static class Builder implements CatchClause.Builder {
            private final List<ParameterizedType> exceptionTypes = new ArrayList<>();
            private String variableName;
            private Block block;

            @Override
            public Builder setBlock(Block block) {
                this.block = block;
                return this;
            }

            @Override
            public CatchClause.Builder addType(ParameterizedType type) {
                exceptionTypes.add(type);
                return this;
            }

            @Override
            public Builder setVariableName(String variableName) {
                this.variableName = variableName;
                return this;
            }

            @Override
            public CatchClause build() {
                return new CatchClauseImpl(exceptionTypes, variableName, block);
            }
        }

        @Override
        public String variableName() {
            return variableName;
        }

        @Override
        public List<ParameterizedType> exceptionTypes() {
            return exceptionTypes;
        }

        @Override
        public Block block() {
            return block;
        }

        @Override
        public int complexity() {
            return exceptionTypes.size() + block.complexity();
        }

        @Override
        public Stream<Element.TypeReference> typesReferenced() {
            return Stream.concat(exceptionTypes.stream()
                            .map(et -> new ElementImpl.TypeReference(et.typeInfo(), true)),
                    block.typesReferenced());
        }

        @Override
        public Stream<Variable> variables(DescendMode descendMode) {
            return block.variables(descendMode);
        }

        @Override
        public void visit(Predicate<Element> predicate) {
            block.visit(predicate);
        }

        @Override
        public void visit(Visitor visitor) {
            block.visit(visitor);
        }

        @Override
        public CatchClause translate(TranslationMap translationMap) {
            List<ParameterizedType> list = exceptionTypes.stream()
                    .map(translationMap::translateType).collect(translationMap.toList(exceptionTypes));
            Block tBlock = (Block) block.translate(translationMap).get(0);
            if (list != exceptionTypes || tBlock != block) {
                return new CatchClauseImpl(list, variableName, tBlock);
            }
            return this;
        }
    }

    public static class Builder extends StatementImpl.Builder<TryStatement.Builder> implements TryStatement.Builder {
        private Block block;
        private Block finallyBlock;
        private final List<CatchClause> catchClauses = new ArrayList<>();
        private final List<LocalVariableCreation> resources = new ArrayList<>();

        @Override
        public TryStatement.Builder setBlock(Block block) {
            this.block = block;
            return this;
        }

        @Override
        public TryStatement.Builder setFinallyBlock(Block block) {
            this.finallyBlock = block;
            return this;
        }

        @Override
        public TryStatement.Builder addCatchClause(CatchClause catchClause) {
            catchClauses.add(catchClause);
            return this;
        }

        @Override
        public TryStatement.Builder addResource(LocalVariableCreation resource) {
            resources.add(resource);
            return this;
        }

        @Override
        public TryStatement build() {
            return new TryStatementImpl(comments, source, annotations, label, List.copyOf(resources), block, List.copyOf(catchClauses), finallyBlock
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TryStatementImpl that)) return false;
        return Objects.equals(block, that.block) && Objects.equals(finallyBlock, that.finallyBlock)
               && Objects.equals(catchClauses, that.catchClauses) && Objects.equals(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, finallyBlock, catchClauses, resources);
    }

    @Override
    public Block finallyBlock() {
        return finallyBlock;
    }

    @Override
    public List<CatchClause> catchClauses() {
        return catchClauses;
    }

    @Override
    public List<LocalVariableCreation> resources() {
        return resources;
    }

    @Override
    public Block block() {
        return block;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            resources.forEach(r -> r.visit(predicate));
            block.visit(predicate);
            catchClauses.forEach(cc -> cc.visit(predicate));
            if (!finallyBlock.isEmpty()) finallyBlock.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            resources.forEach(e -> e.visit(visitor));
            visitor.startSubBlock(0);
            block.visit(visitor);
            visitor.endSubBlock(0);
            int i = 1;
            for (CatchClause cc : catchClauses) {
                visitor.startSubBlock(i);
                cc.visit(visitor);
                cc.visit(visitor);
                visitor.endSubBlock(i);
                i++;
            }
            if (!finallyBlock.isEmpty()) {
                visitor.startSubBlock(i);
                finallyBlock.visit(visitor);
                visitor.endSubBlock(i);
            }
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification).add(KeywordImpl.TRY);
        if (!resources.isEmpty()) {
            outputBuilder.add(SymbolEnum.LEFT_PARENTHESIS)
                    .add(resources.stream().map(expression -> expression
                            .print(qualification)).collect(OutputBuilderImpl.joining(SymbolEnum.SEMICOLON)))
                    .add(SymbolEnum.RIGHT_PARENTHESIS);
        }
        outputBuilder.add(block.print(qualification));
        int i = 1;
        for (CatchClause cc : catchClauses) {
            outputBuilder.add(KeywordImpl.CATCH)
                    .add(SymbolEnum.LEFT_PARENTHESIS)
                    .add(cc.exceptionTypes().stream()
                            .map(t -> t.print(qualification, false, DiamondEnum.NO))
                            .collect(OutputBuilderImpl.joining(SymbolEnum.PIPE)))
                    .add(SpaceEnum.ONE)
                    .add(new TextImpl(cc.variableName()))
                    .add(SymbolEnum.RIGHT_PARENTHESIS)
                    .add(cc.block().print(qualification));
            i++;
        }
        if (!finallyBlock.isEmpty()) {
            outputBuilder
                    .add(KeywordImpl.FINALLY)
                    .add(finallyBlock.print(qualification));
        }
        return outputBuilder;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(resources.stream().flatMap(r -> r.variables(descendMode)),
                Stream.concat(block.variables(descendMode), Stream.concat(
                        catchClauses.stream().flatMap(cc -> cc.variables(descendMode)),
                        finallyBlock.variables(descendMode))));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.concat(resources.stream().flatMap(LocalVariableCreation::typesReferenced),
                Stream.concat(block.typesReferenced(), Stream.concat(catchClauses.stream().flatMap(CatchClause::typesReferenced),
                        finallyBlock.typesReferenced())));
    }

    @Override
    public List<Block> otherBlocks() {
        return Stream.concat(catchClauses.stream().map(CatchClause::block).filter(b -> !b.isEmpty()),
                finallyBlock.isEmpty() ? Stream.of() : Stream.of(finallyBlock)).toList();
    }

    @Override
    public boolean hasSubBlocks() {
        return true;
    }

    @Override
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (haveDirectTranslation(direct, this)) return direct;
        Block tMain = (Block) block.translate(translationMap).get(0);
        Block tFinally = (Block) finallyBlock.translate(translationMap).get(0);
        List<LocalVariableCreation> tResources = resources.stream()
                .map(st -> (LocalVariableCreation) st.translate(translationMap).get(0))
                .collect(translationMap.toList(resources));
        List<CatchClause> tCatch = catchClauses.stream()
                .map(cc -> cc.translate(translationMap))
                .collect(translationMap.toList(catchClauses));
        if (tMain != block || tFinally != finallyBlock) {
            return List.of(new TryStatementImpl(comments(), source(), annotations(), label(), tResources,
                    tMain, tCatch, tFinally));
        }
        return List.of(this);
    }
}
