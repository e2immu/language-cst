package org.e2immu.language.cst.api.statement;

import org.e2immu.annotation.Fluent;

import java.util.List;
import java.util.stream.Stream;

public interface Block extends Statement {
    default int size() {
        return statements().size();
    }

    List<Statement> statements();

    interface Builder extends Statement.Builder<Builder> {

        Block build();

        @Fluent
        Builder addStatements(List<Statement> statements);

        @Fluent
        Builder addStatement(Statement statement);

        @Fluent
        Builder addStatements(int index, List<Statement> statements);

        @Fluent
        Builder addStatement(int index, Statement statement);

        List<Statement> statements();
    }

    default boolean isEmpty() {
        return statements().isEmpty();
    }

    /*
    Remove statement from statements list in block.
    This method descends into Block statements!
     */
    Block remove(Statement toRemove);

    String NAME = "block";

    @Override
    default String name() {
        return NAME;
    }
}
