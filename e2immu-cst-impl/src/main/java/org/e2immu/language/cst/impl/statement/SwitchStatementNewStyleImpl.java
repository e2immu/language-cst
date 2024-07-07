package org.e2immu.language.cst.impl.statement;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.statement.SwitchStatementNewStyle;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SwitchStatementNewStyleImpl extends StatementImpl implements SwitchStatementNewStyle {
    private final Expression selector;
    private final List<Entry> entries;

    public SwitchStatementNewStyleImpl(List<Comment> comments, Source source, List<AnnotationExpression> annotations,
                                       String label, Expression selector, List<Entry> entries) {
        super(comments, source, annotations,
                10 + selector.complexity() + entries.stream().mapToInt(Entry::complexity).sum(), label);
        this.selector = selector;
        this.entries = entries;
    }

    @Override
    public Expression expression() {
        return selector;
    }

    @Override
    public List<Entry> entries() {
        return entries;
    }

    @Override
    public boolean hasSubBlocks() {
        return !entries.isEmpty();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        selector.visit(predicate);
        int i = 0;
        for (Entry entry : entries) {
            entry.conditions().forEach(e -> e.visit(predicate));
            if (entry.patternVariable() != null) entry.patternVariable().visit(predicate);
            entry.whenExpression().visit(predicate);
            entry.statement().visit(predicate);
            i++;
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            selector.visit(visitor);
            int i = 0;
            for (Entry entry : entries) {
                entry.conditions().forEach(e -> e.visit(visitor));
                if (entry.patternVariable() != null) entry.patternVariable().visit(visitor);
                entry.whenExpression().visit(visitor);
                visitor.startSubBlock(i);
                entry.statement().visit(visitor);
                visitor.endSubBlock(i);
                i++;
            }
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = new OutputBuilderImpl().add(KeywordImpl.SWITCH)
                .add(SymbolEnum.LEFT_PARENTHESIS)
                .add(selector.print(qualification))
                .add(SymbolEnum.RIGHT_PARENTHESIS)
                .add(SymbolEnum.LEFT_BRACE);
        GuideImpl.GuideGenerator guideGenerator = GuideImpl.generatorForBlock();
        outputBuilder.add(guideGenerator.start());
        int i = 0;
        for (Entry entry : entries) {
            if (i > 0) outputBuilder.add(guideGenerator.mid());
            outputBuilder.add(entry.print(qualification));
            i++;
        }
        return outputBuilder.add(guideGenerator.end()).add(SymbolEnum.RIGHT_BRACE);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(selector.variables(descendMode),
                entries.stream().flatMap(e -> e.variables(descendMode)));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.empty();
    }

    public static class EntryImpl implements Entry {
        private final List<Expression> conditions;
        private final LocalVariable patternVariable;
        private final Expression whenExpression;
        private final Statement statement;

        public EntryImpl(List<Expression> conditions, LocalVariable patternVariable, Expression whenExpression, Statement statement) {
            this.conditions = conditions;
            this.patternVariable = patternVariable;
            this.whenExpression = whenExpression;
            this.statement = statement;
        }

        @Override
        public int complexity() {
            return (patternVariable != null ? 1 : 0)
                   + conditions.stream().mapToInt(Expression::complexity).sum()
                   + whenExpression.complexity() + statement().complexity();
        }

        @Override
        public List<Expression> conditions() {
            return conditions;
        }

        @Override
        public LocalVariable patternVariable() {
            return patternVariable;
        }

        @Override
        public Expression whenExpression() {
            return whenExpression;
        }

        @Override
        public Statement statement() {
            return statement;
        }

        @Override
        public OutputBuilder print(Qualification qualification) {
            OutputBuilder outputBuilder = new OutputBuilderImpl();
            boolean containsDefault = conditions.stream().anyMatch(Expression::isEmpty);
            if (containsDefault) {
                outputBuilder.add(KeywordImpl.DEFAULT);
                if (conditions.size() > 1) {
                    outputBuilder.add(SymbolEnum.COMMA).add(KeywordImpl.CASE);
                }
            } else {
                outputBuilder.add(KeywordImpl.CASE);
            }
            outputBuilder.add(SpaceEnum.ONE);
            boolean first = true;
            for (Expression condition : conditions) {
                if (!condition.isEmpty()) {
                    if (first) {
                        first = false;
                    } else {
                        outputBuilder.add(SymbolEnum.COMMA);
                    }
                    outputBuilder.add(condition.print(qualification));
                }
            }
            outputBuilder.add(SymbolEnum.LAMBDA);
            outputBuilder.add(statement().print(qualification));
            return outputBuilder;
        }

        @Override
        public Entry translate(TranslationMap translationMap) {
            throw new UnsupportedOperationException("NYI");
        }

        @Override
        public Stream<Variable> variables(DescendMode descendMode) {
            return Stream.concat(whenExpression.variables(descendMode), statement.variables(descendMode));
        }
    }

    public static class BuilderImpl extends StatementImpl.Builder<SwitchStatementNewStyle.Builder>
            implements SwitchStatementNewStyle.Builder {
        private Expression selector;
        private final List<Entry> entries = new ArrayList<>();

        @Override
        public SwitchStatementNewStyle.Builder setSelector(Expression selector) {
            this.selector = selector;
            return this;
        }

        @Override
        public SwitchStatementNewStyle.Builder addSwitchEntries(Collection<Entry> switchEntries) {
            this.entries.addAll(switchEntries);
            return this;
        }

        @Override
        public SwitchStatementNewStyle build() {
            return new SwitchStatementNewStyleImpl(comments, source, annotations, label, selector, List.copyOf(entries));
        }
    }

    public static class EntryBuilderImpl implements EntryBuilder {
        private final List<Expression> conditions = new ArrayList<>();
        private LocalVariable patternVariable;
        private Expression whenExpression;
        private Statement statement;

        @Override
        public EntryBuilder addConditions(Collection<Expression> expressions) {
            this.conditions.addAll(expressions);
            return this;
        }

        @Override
        public EntryBuilder setStatement(Statement statement) {
            this.statement = statement;
            return this;
        }

        @Override
        public EntryBuilder setPatternVariable(LocalVariable patternVariable) {
            this.patternVariable = patternVariable;
            return this;
        }

        @Override
        public EntryBuilder setWhenExpression(Expression whenExpression) {
            this.whenExpression = whenExpression;
            return this;
        }

        @Override
        public Entry build() {
            return new EntryImpl(List.copyOf(conditions), patternVariable, whenExpression, statement);
        }
    }
}
