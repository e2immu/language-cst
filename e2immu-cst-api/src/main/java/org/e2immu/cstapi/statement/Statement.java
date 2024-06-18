package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.annotation.NotNull;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.translate.TranslationMap;

import java.util.List;
import java.util.stream.Stream;

public interface Statement extends Element {

    // a label to possibly jump to
    String label();

    // many have one
    default Block block() {
        return null;
    }

    // all other blocks, except for block()
    @NotNull
    default List<Block> otherBlocks() {
        return List.of();
    }

    // many have one; if they have one, this is the one (if they have many, this is the first)
    default Expression expression() {
        return null;
    }

    // return all blocks
    @NotNull
    default Stream<Block> subBlockStream() {
        return Stream.concat(Stream.ofNullable(block()), otherBlocks().stream());
    }

    interface Builder<B extends Builder<?>> extends Element.Builder<B> {
        @Fluent
        B setLabel(String label);
    }

    default List<Statement> translate(TranslationMap translationMap) {
        return List.of(this);
    }

    // from analysis
    default boolean alwaysEscapes() {
        return false;
    }
}
