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

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("{3=[14, 36, 59, 75, 80], 4=[77, 78]}", output.possibleSplits().toString());
        Formatter2Impl formatter2 = new Formatter2Impl(runtime, options);
        assertEquals(expect, formatter2.write(outputBuilder));
    }

    @Test
    public void test2() {
        OutputBuilder outputBuilder = createExample1();
        Formatter2Impl.Block block = new Formatter2Impl.Block(1, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(70).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = """
                package a.b.c; import java.util.Set; import java.util.List;
                    record Record() {  }\s""";
        assertEquals(expect, output.string());
        assertTrue(output.extraLines());
        assertEquals(25, output.endPos());
        // the newline in expect is at position 59; indent = 4, so 'record' starts at 63
        // note that the space between record and Record is 'ONE', which does not allow a split
        assertEquals("{3=[79, 84], 4=[81, 82]}", output.possibleSplits().toString());
    }


    @Test
    public void test3() {
        OutputBuilder outputBuilder = createExample1();
        Formatter2Impl.Block block = new Formatter2Impl.Block(1, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(50).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = """
                package a.b.c; import java.util.Set;
                    import java.util.List; record Record() {\s
                    }\s""";
        assertEquals(expect, output.string());
        assertTrue(output.extraLines());
        assertEquals(6, output.endPos());
        // the newline in expect is at position 59; indent = 4, so 'record' starts at 63
        // note that the space between record and Record is 'ONE', which does not allow a split
        assertEquals("{3=[6]}", output.possibleSplits().toString());
    }
}
