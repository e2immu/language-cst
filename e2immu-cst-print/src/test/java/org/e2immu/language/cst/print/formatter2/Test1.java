package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.Formatter;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.runtime.RuntimeImpl;
import org.e2immu.language.cst.print.Formatter2Impl;
import org.e2immu.language.cst.print.FormattingOptionsImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test1 {
    private final Runtime runtime = new RuntimeImpl();

    private OutputBuilder createExample1() {
        GuideImpl.GuideGenerator gg = GuideImpl.generatorForParameterDeclaration();
        GuideImpl.GuideGenerator gg2 = GuideImpl.generatorForBlock();

        return new OutputBuilderImpl()
                .add(KeywordImpl.PUBLIC).add(SpaceEnum.ONE)
                .add(new TextImpl("int")).add(SpaceEnum.ONE)
                .add(new TextImpl("method"))
                .add(SymbolEnum.LEFT_PARENTHESIS)
                .add(gg.start()).add(new TextImpl("int")).add(SpaceEnum.ONE).add(new TextImpl("p1")).add(SymbolEnum.COMMA)
                .add(gg.mid()).add(new TextImpl("int")).add(SpaceEnum.ONE).add(new TextImpl("p2"))
                .add(gg.end())
                .add(SymbolEnum.RIGHT_PARENTHESIS)
                .add(SymbolEnum.LEFT_BRACE)
                .add(gg2.start()).add(KeywordImpl.RETURN).add(SpaceEnum.ONE)
                .add(new TextImpl("p1")).add(SymbolEnum.binaryOperator("+")).add(new TextImpl("p2")).add(SymbolEnum.SEMICOLON)
                .add(gg2.end())
                .add(SymbolEnum.RIGHT_BRACE);
    }


    @Test
    public void test1() {
        OutputBuilder outputBuilder = createExample1();
        Formatter2Impl formatter = new Formatter2Impl(runtime, new FormattingOptionsImpl.Builder().build());
        String out = formatter.minimal(outputBuilder);
        String expect = """
                """;
        assertEquals(expect, out);
    }
}
