package org.e2immu.language.cst.impl.statement;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Block;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.statement.SwitchStatementOldStyle;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SwitchStatementOldStyleImpl extends StatementImpl implements SwitchStatementOldStyle {
    private final Expression selector;
    private final Block block;
    private final List<SwitchLabel> switchLabels;

    public SwitchStatementOldStyleImpl(List<Comment> comments, Source source, List<AnnotationExpression> annotations,
                                       String label, Expression selector, Block block, List<SwitchLabel> switchLabels) {
        super(comments, source, annotations,
                10 + block.complexity() + selector.complexity() + switchLabels.size(), label);
        this.selector = selector;
        this.block = block;
        this.switchLabels = switchLabels;
    }

    @Override
    public Statement withBlocks(List<Block> tSubBlocks) {
        return new SwitchStatementOldStyleImpl(comments(), source(), annotations(), label(), selector,
                tSubBlocks.get(0), switchLabels);
    }

    public static class SwitchLabelImpl implements SwitchLabel {
        private final Expression literal;
        private final int startFromPosition;
        private final LocalVariable patternVariable;
        private final Expression whenExpression;

        public SwitchLabelImpl(Expression literal, int startFromPosition, LocalVariable patternVariable, Expression whenExpression) {
            this.literal = literal;
            this.startFromPosition = startFromPosition;
            this.patternVariable = patternVariable;
            this.whenExpression = whenExpression;
        }

        @Override
        public OutputBuilder print(Qualification qualification) {
            OutputBuilder outputBuilder = new OutputBuilderImpl();
            if (literal.isEmpty()) {
                outputBuilder.add(KeywordImpl.DEFAULT);
            } else {
                outputBuilder.add(KeywordImpl.CASE)
                        .add(SpaceEnum.ONE)
                        .add(literal.print(qualification));
            }
            if (patternVariable != null) {
                outputBuilder.add(SpaceEnum.ONE).add(patternVariable.print(qualification));

                if (!whenExpression.isEmpty()) {
                    outputBuilder.add(SpaceEnum.ONE).add(whenExpression.print(qualification));
                }
            }
            return outputBuilder.add(SymbolEnum.COLON_LABEL);
        }

        @Override
        public int startFromPosition() {
            return startFromPosition;
        }

        @Override
        public Expression literal() {
            return literal;
        }

        @Override
        public Expression whenExpression() {
            return whenExpression;
        }

        @Override
        public LocalVariable patternVariable() {
            return patternVariable;
        }

        @Override
        public SwitchLabel translate(TranslationMap translationMap) {
            Expression trLiteral = literal.translate(translationMap);
            LocalVariable trPattern = patternVariable == null ? null
                    : (LocalVariable) translationMap.translateVariable(patternVariable);
            Expression trWhen = whenExpression.translate(translationMap);
            if (trLiteral == literal && trPattern == patternVariable && trWhen == whenExpression) return this;
            return new SwitchLabelImpl(trLiteral, startFromPosition, trPattern, trWhen);
        }
    }

    public static class Builder extends StatementImpl.Builder<SwitchStatementOldStyle.Builder> implements SwitchStatementOldStyle.Builder {
        private Expression selector;
        private Block block;
        private final List<SwitchLabel> switchLabels = new ArrayList<>();

        @Override
        public SwitchStatementOldStyle.Builder setSelector(Expression selector) {
            this.selector = selector;
            return this;
        }

        @Override
        public SwitchStatementOldStyle.Builder setBlock(Block block) {
            this.block = block;
            return this;
        }

        @Override
        public SwitchStatementOldStyle.Builder addSwitchLabels(Collection<SwitchLabel> switchLabels) {
            this.switchLabels.addAll(switchLabels);
            return this;
        }

        @Override
        public SwitchStatementOldStyle build() {
            return new SwitchStatementOldStyleImpl(comments, source, annotations, label, selector, block, switchLabels);
        }
    }

    @Override
    public Expression expression() {
        return selector;
    }

    @Override
    public Block block() {
        return block;
    }

    @Override
    public List<SwitchLabel> switchLabels() {
        return switchLabels;
    }


    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            selector.visit(predicate);
            block.visit(predicate);
            switchLabels.forEach(sl -> sl.literal().visit(predicate));
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            selector.visit(visitor);
            visitor.startSubBlock(0);
            switchLabels.forEach(sl -> sl.literal().visit(visitor));
            block.visit(visitor);
            visitor.endSubBlock(0);
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification).add(KeywordImpl.SWITCH)
                .add(SymbolEnum.LEFT_PARENTHESIS).add(selector.print(qualification)).add(SymbolEnum.RIGHT_PARENTHESIS);
        outputBuilder.add(SymbolEnum.LEFT_BRACE);
        if (!block.isEmpty()) {
            GuideImpl.GuideGenerator guideGenerator = GuideImpl.generatorForBlock();
            outputBuilder.add(guideGenerator.start());
            print(qualification, outputBuilder, guideGenerator, block, switchLabelMap());
            outputBuilder.add(guideGenerator.end());
        }
        return outputBuilder.add(SymbolEnum.RIGHT_BRACE);
    }

    private void print(Qualification qualification, OutputBuilder outputBuilder,
                       GuideImpl.GuideGenerator gg, Block block, Map<String, List<SwitchLabel>> idToLabels) {
        GuideImpl.GuideGenerator statementGg = null;
        boolean notFirst = false;
        boolean notFirstInCase = false;
        for (Statement statement : block.statements()) {
            String index = statement.source().index();
            if (idToLabels.containsKey(index)) {
                if (statementGg != null) {
                    outputBuilder.add(statementGg.end());
                }
                if (!notFirst) notFirst = true;
                else outputBuilder.add(gg.mid());
                for (SwitchStatementOldStyle.SwitchLabel switchLabel : idToLabels.get(index)) {
                    outputBuilder.add(switchLabel.print(qualification));
                    gg.mid();
                }
                statementGg = GuideImpl.generatorForBlock();
                outputBuilder.add(statementGg.start());
                notFirstInCase = false;
            }
            assert statementGg != null;
            if (!notFirstInCase) notFirstInCase = true;
            else outputBuilder.add(statementGg.mid());

            outputBuilder.add(statement.print(qualification));
        }
        if (statementGg != null) {
            outputBuilder.add(statementGg.end());
        }
    }

    @Override
    public Map<String, List<SwitchLabel>> switchLabelMap() {
        Map<String, List<SwitchLabel>> res = new HashMap<>();
        int i = 0;
        int labelIndex = 0;
        for (Statement statement : block.statements()) {
            while (labelIndex < switchLabels.size() && switchLabels.get(labelIndex).startFromPosition() == i) {
                res.computeIfAbsent(statement.source().index(), s -> new ArrayList<>()).add(switchLabels.get(labelIndex));
                labelIndex++;
            }
            i++;
        }
        return res;
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
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (hasBeenTranslated(direct, this)) return direct;

        Expression translatedExpression = selector.translate(translationMap);
        List<SwitchLabel> translatedLabels = switchLabels.stream()
                .map(l -> l.translate(translationMap))
                .collect(Collectors.toList());
        return List.of(new SwitchStatementOldStyleImpl(comments(), source(), annotations(), label(), translatedExpression,
                ensureBlock(block.translate(translationMap)), translatedLabels));
    }

    @Override
    public boolean hasSubBlocks() {
        return true;
    }
}
