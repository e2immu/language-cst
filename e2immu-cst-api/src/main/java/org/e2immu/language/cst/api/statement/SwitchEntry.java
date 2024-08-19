package org.e2immu.language.cst.api.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.Element;
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

public interface SwitchEntry {
    int complexity();

    // EmptyExpression for 'default', NullConstant for 'null'

    List<Expression> conditions();
    // or null, when absent (Java 21)

    LocalVariable patternVariable();

    Stream<Element.TypeReference> typesReferenced();

    // EmptyExpression when absent (Java 21)
    Expression whenExpression();

    Statement statement();

    OutputBuilder print(Qualification qualification);

    SwitchEntry translate(TranslationMap translationMap);

    Stream<Variable> variables(DescendMode descendMode);

    SwitchEntry withStatement(Statement statement);

    interface Builder {
        @Fluent
        Builder addConditions(Collection<Expression> expressions);

        @Fluent
        Builder setStatement(Statement statement);

        @Fluent
        Builder setPatternVariable(LocalVariable patternVariable);

        @Fluent
        Builder setWhenExpression(Expression whenExpression);

        SwitchEntry build();
    }
}
