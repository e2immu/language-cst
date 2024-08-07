package org.e2immu.language.cst.api.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface TryStatement extends Statement {

    interface CatchClause {
        List<ParameterizedType> exceptionTypes();

        CatchClause translate(TranslationMap translationMap);

        String variableName();

        Block block();

        int complexity();

        Stream<TypeReference> typesReferenced();

        Stream<Variable> variables(DescendMode descendMode);

        void visit(Predicate<Element> predicate);

        void visit(Visitor visitor);

        interface Builder {
            @Fluent
            Builder setBlock(Block block);

            @Fluent
            Builder addType(ParameterizedType type);

            @Fluent
            Builder setVariableName(String name);

            CatchClause build();
        }
    }

    Block finallyBlock();

    List<CatchClause> catchClauses();

    List<LocalVariableCreation> resources();

    interface Builder extends Statement.Builder<Builder> {

        @Fluent
        Builder setBlock(Block block);

        @Fluent
        Builder setFinallyBlock(Block block);

        @Fluent
        Builder addCatchClause(CatchClause catchClause);

        @Fluent
        Builder addResource(LocalVariableCreation localVariableCreation);

        TryStatement build();
    }
}
