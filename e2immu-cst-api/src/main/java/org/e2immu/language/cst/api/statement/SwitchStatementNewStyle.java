package org.e2immu.language.cst.api.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface SwitchStatementNewStyle extends Statement {

    // selector == expression()
    // block = null
    // otherBlocks = each of the individual blocks?

    interface Entry {
        int complexity();

        // EmptyExpression for 'default', NullConstant for 'null'

        List<Expression> conditions();
        // or null, when absent (Java 21)

        LocalVariable patternVariable();


        // EmptyExpression when absent (Java 21)
        Expression whenExpression();

        Statement statement();

        OutputBuilder print(Qualification qualification);

        Entry translate(TranslationMap translationMap);

        Stream<Variable> variables(DescendMode descendMode);
    }

    List<Entry> entries();

    interface Builder extends Statement.Builder<Builder> {

        @Fluent
        Builder setSelector(Expression selector);

        @Fluent
        Builder addSwitchEntries(Collection<Entry> switchEntries);

        SwitchStatementNewStyle build();
    }

    interface EntryBuilder {
        @Fluent
        EntryBuilder addConditions(Collection<Expression> expressions);

        @Fluent
        EntryBuilder setStatement(Statement statement);

        @Fluent
        EntryBuilder setPatternVariable(LocalVariable patternVariable);

        @Fluent
        EntryBuilder setWhenExpression(Expression whenExpression);

        Entry build();
    }
}
