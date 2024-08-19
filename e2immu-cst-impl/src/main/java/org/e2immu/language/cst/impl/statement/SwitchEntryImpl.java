package org.e2immu.language.cst.impl.statement;

import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Block;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.statement.SwitchEntry;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.*;

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
        throw new UnsupportedOperationException("NYI");
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
