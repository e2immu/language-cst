package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.Formatter;
import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SpaceEnum;
import org.e2immu.language.cst.impl.output.SymbolEnum;
import org.e2immu.language.cst.impl.output.TextImpl;
import org.e2immu.language.cst.impl.runtime.RuntimeImpl;
import org.e2immu.language.cst.print.FormattingOptionsImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestBlockPrinter1 {
    private final Runtime runtime = new RuntimeImpl();

    private static OutputBuilder createExample1() {
        return new OutputBuilderImpl().add(new TextImpl("package"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("a.b.c"))
                .add(SymbolEnum.SEMICOLON)
                .add(new TextImpl("import"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("java.util.Set"))
                .add(SymbolEnum.SEMICOLON)
                .add(new TextImpl("import"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("java.util.List"))
                .add(SymbolEnum.SEMICOLON)
                .add(new TextImpl("record"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("Record"))
                .add(SymbolEnum.OPEN_CLOSE_PARENTHESIS)
                .add(SymbolEnum.LEFT_BRACE)
                .add(SymbolEnum.RIGHT_BRACE);
    }

    @Test
    public void test1() {
        OutputBuilder outputBuilder = createExample1();
        Formatter2Impl.Block block = new Formatter2Impl.Block(0, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(120).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = "package a.b.c; import java.util.Set; import java.util.List; record Record() {  } ";
        assertEquals(expect, output.string());
        assertFalse(output.extraLines());
        assertEquals(81, output.endPos());
        assertEquals("", output.possibleSplits().toString());
    }
}
