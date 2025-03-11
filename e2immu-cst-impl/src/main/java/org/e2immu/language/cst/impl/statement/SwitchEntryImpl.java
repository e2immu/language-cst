package org.e2immu.language.cst.impl.statement;

import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.statement.SwitchEntry;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.util.internal.util.ListUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class SwitchEntryImpl implements SwitchEntry {
    private final List<Expression> conditions;
    private final LocalVariable patternVariable;
    private final Expression whenExpression;
    private final Statement statement;

    public SwitchEntryImpl(List<Expression> conditions, LocalVariable patternVariable, Expression whenExpression, Statement statement) {
        this.conditions = conditions;
        this.patternVariable = patternVariable;
        this.whenExpression = whenExpression;
        this.statement = statement;
    }

    @Override
    public int compareTo(SwitchEntry o) {
        return ListUtil.compare(conditions, o.conditions());
    }

    @Override
    public SwitchEntry withStatement(Statement statement) {
        return new SwitchEntryImpl(conditions, patternVariable, whenExpression, statement);
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
        boolean first = true;
        if (containsDefault) {
            outputBuilder.add(KeywordImpl.DEFAULT);
            first = false;
            if (conditions.size() > 1) {
                outputBuilder.add(SymbolEnum.COMMA).add(KeywordImpl.CASE);
            }
        } else {
            outputBuilder.add(KeywordImpl.CASE);
            outputBuilder.add(SpaceEnum.ONE);
        }
        for (Expression condition : conditions) {
            if (!condition.isEmpty()) {
                if (first) {
                    first = false;
                } else {
                    outputBuilder.add(SymbolEnum.COMMA);
                }
                outputBuilder.add(condition.print(QualificationImpl.SIMPLE_ONLY));
            }
        }
        outputBuilder.add(SymbolEnum.LAMBDA);
        outputBuilder.add(statement().print(qualification));
        return outputBuilder;
    }

    @Override
    public SwitchEntry translate(TranslationMap translationMap) {
        List<Expression> tConditions = conditions.stream().map(c -> c.translate(translationMap))
                .collect(translationMap.toList(conditions));
        LocalVariable tPattern = patternVariable == null ? null
                : (LocalVariable) translationMap.translateVariable(patternVariable);
        Expression tWhen = whenExpression == null ? null : whenExpression.translate(translationMap);
        List<Statement> tStatements = statement.translate(translationMap);
        Statement tStatement = tStatements.isEmpty() ? null : tStatements.get(0);
        if (tConditions == conditions && tPattern == patternVariable && tWhen == whenExpression
            && tStatement == statement) {
            return this;
        }
        if (tStatement == null) return null; // a way for the entry to disappear
        return new SwitchEntryImpl(tConditions, tPattern, tWhen, tStatement);
    }

    @Override
    public SwitchEntry rewire(InfoMap infoMap) {
        return new SwitchEntryImpl(conditions.stream().map(e -> e.rewire(infoMap)).toList(),
                patternVariable == null ? null : (LocalVariable) patternVariable.rewire(infoMap),
                whenExpression.rewire(infoMap), statement.rewire(infoMap));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(whenExpression.variables(descendMode), statement.variables(descendMode));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.concat(whenExpression.typesReferenced(), statement.typesReferenced());
    }

    public static class EntryBuilderImpl implements Builder {
        private final List<Expression> conditions = new ArrayList<>();
        private LocalVariable patternVariable;
        private Expression whenExpression;
        private Statement statement;

        @Override
        public Builder addConditions(Collection<Expression> expressions) {
            this.conditions.addAll(expressions);
            return this;
        }

        @Override
        public Builder setStatement(Statement statement) {
            this.statement = statement;
            return this;
        }

        @Override
        public Builder setPatternVariable(LocalVariable patternVariable) {
            this.patternVariable = patternVariable;
            return this;
        }

        @Override
        public Builder setWhenExpression(Expression whenExpression) {
            this.whenExpression = whenExpression;
            return this;
        }

        @Override
        public SwitchEntry build() {
            return new SwitchEntryImpl(List.copyOf(conditions), patternVariable, whenExpression, statement);
        }
    }
}
