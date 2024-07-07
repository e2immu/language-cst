package org.e2immu.language.cst.api.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface SwitchStatementOldStyle extends Statement {

    // selector == expression()

    interface SwitchLabel {
        int startFromPosition();

        Expression literal();

        // null when absent
        LocalVariable patternVariable();

        // EmptyExpression when absent (Java 21)
        Expression whenExpression();

        OutputBuilder print(Qualification qualification);

        SwitchLabel translate(TranslationMap translationMap);
    }

    List<SwitchLabel> switchLabels();

    interface Builder extends Statement.Builder<Builder> {

        @Fluent
        Builder setSelector(Expression selector);

        @Fluent
        Builder setBlock(Block block);

        @Fluent
        Builder addSwitchLabels(Collection<SwitchLabel> switchLabels);

        SwitchStatementOldStyle build();
    }
}
