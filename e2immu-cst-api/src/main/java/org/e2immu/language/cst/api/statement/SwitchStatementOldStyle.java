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
import org.e2immu.language.cst.api.variable.Variable;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface SwitchStatementOldStyle extends Statement {

    // selector == expression()

    interface SwitchLabel {
        OutputBuilder print(Qualification qualification);

        int startFromPosition();
       Expression literal();

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
