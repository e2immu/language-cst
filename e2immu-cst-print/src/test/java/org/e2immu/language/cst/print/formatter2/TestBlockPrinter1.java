package org.e2immu.language.cst.print.formatter2;

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
    public void test1a() {
        OutputBuilder outputBuilder = createExample1();
        Formatter2Impl.Block block = new Formatter2Impl.Block(0, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(120).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = "package a.b.c; import java.util.Set; import java.util.List; record Record() { }";
        assertEquals(expect + " ", output.string());
        assertFalse(output.extraLines());
        assertEquals(80, output.endPos());
        // 14, 36, 59 = space after ; 75 = space after ); 77 = space after {
        assertEquals("{3={14=false, 36=false, 59=false, 75=false}, 4={77=false}}", output.splitInfo().map().toString());
        Formatter2Impl formatter2 = new Formatter2Impl(runtime, options);
        assertEquals(expect + "\n", formatter2.write(outputBuilder));
    }

    @Test
    public void test1b() {
        OutputBuilder outputBuilder = createExample1();
        Formatter2Impl.Block block = new Formatter2Impl.Block(1, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(70).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = """
                package a.b.c; import java.util.Set; import java.util.List;
                    record Record() { }\s""";
        assertEquals(expect, output.string());
        assertTrue(output.extraLines());
        assertEquals(24, output.endPos());
        // the newline in expect is at position 59; indent = 4, so 'record' starts at 63
        // note that the space between record and Record is 'ONE', which does not allow a split
        assertEquals("{3={79=false}, 4={81=false}}", output.splitInfo().map().toString());
        Formatter2Impl formatter2 = new Formatter2Impl(runtime, options);
        String formatted = """
                package a.b.c; import java.util.Set; import java.util.List;
                record Record() { }
                """;
        assertEquals(formatted, formatter2.write(outputBuilder));
    }


    @Test
    public void test1c() {
        OutputBuilder outputBuilder = createExample1();
        Formatter2Impl.Block block = new Formatter2Impl.Block(1, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(50).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = """
                package a.b.c; import java.util.Set;
                    import java.util.List; record Record() {
                    }\s""";
        assertEquals(expect, output.string());
        assertTrue(output.extraLines());
        assertEquals(6, output.endPos());
        assertEquals("{}", output.splitInfo().map().toString());
    }


    @Test
    public void test1d() {
        OutputBuilder outputBuilder = createExample1();
        Formatter2Impl.Block block = new Formatter2Impl.Block(1, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(55).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = """
                package a.b.c; import java.util.Set;
                    import java.util.List; record Record() { }\s""";
        assertEquals(expect, output.string());
        assertTrue(output.extraLines());
        assertEquals(47, output.endPos());
        assertEquals("{3={63=false, 79=false}, 4={81=false}}", output.splitInfo().map().toString());
    }


    private static OutputBuilder createExample2() {
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
                .add(SpaceEnum.NEWLINE)
                .add(SymbolEnum.SINGLE_LINE_COMMENT)
                .add(new TextImpl("this is a comment"))
                .add(SpaceEnum.NEWLINE)
                .add(SymbolEnum.SINGLE_LINE_COMMENT)
                .add(new TextImpl("this is a second comment"))
                .add(SpaceEnum.NEWLINE)
                .add(new TextImpl("record"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("Record"))
                .add(SymbolEnum.OPEN_CLOSE_PARENTHESIS)
                .add(SymbolEnum.LEFT_BRACE)
                .add(SymbolEnum.RIGHT_BRACE);
    }

    @Test
    public void test2a() {
        OutputBuilder outputBuilder = createExample2();
        Formatter2Impl.Block block = new Formatter2Impl.Block(1, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(120).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = """
                package a.b.c; import java.util.Set; import java.util.List;
                    //this is a comment
                    //this is a second comment
                    record Record() { }\s""";
        assertEquals(expect, output.string());
        assertFalse(output.extraLines());
        assertEquals(139, output.endPos());
        assertEquals("{3={134=false}, 4={136=false}}", output.splitInfo().map().toString());
    }

}
