package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;

import java.util.List;
import java.util.stream.Stream;

public interface Block extends Statement {
    List<Statement> statements();

    interface Builder extends Statement.Builder<Builder> {

        Block build();

        @Fluent
        Builder addStatements(List<Statement> statements);

        @Fluent
        Builder addStatement(Statement statement);
    }

    default boolean isEmpty() {
        return statements().isEmpty();
    }

    @Override
    default Block block() {
        return this;
    }

    @Override
    default Stream<Block> subBlockStream() {
        return Stream.of(this);
    }
}
