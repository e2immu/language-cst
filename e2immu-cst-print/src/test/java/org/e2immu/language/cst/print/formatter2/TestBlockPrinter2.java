package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.Formatter;
import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.runtime.RuntimeImpl;
import org.e2immu.language.cst.print.FormattingOptionsImpl;
import org.e2immu.language.cst.print.formatter.TestFormatter1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBlockPrinter2 {
    private final Runtime runtime = new RuntimeImpl();

    @Test
    public void test1a() {
        OutputBuilder outputBuilder = TestFormatter1.createExample1();
        Formatter2Impl.Block block = new Formatter2Impl.Block(1, outputBuilder.list(), null);
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(70).setSpacesInTab(4).build();
        BlockPrinter blockPrinter = new BlockPrinter();
        BlockPrinter.Output output = blockPrinter.write(block, options);
        String expect = "public int method(int p1, int p2) { return p1 + p2; } ";
        assertEquals(expect, output.string());

        Formatter formatter = new Formatter2Impl(runtime, options);
        String string = formatter.write(outputBuilder);
        String formatted = "public int method(int p1, int p2) { return p1 + p2; }\n";
        assertEquals(formatted, string);
    }

    @Test
    public void test1b() {
        OutputBuilder outputBuilder = TestFormatter1.createExample1();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(30).setSpacesInTab(4).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String string = formatter.write(outputBuilder);
        String expect = """
                public int method(
                    int p1,
                    int p2) {
                    return p1 + p2;
                }
                """;
        assertEquals(expect, string);
    }

    @Test
    public void test2a() {
        OutputBuilder outputBuilder = TestFormatter1.createExample2();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(20).setSpacesInTab(2).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String string = formatter.write(outputBuilder);
        String expect = """
                public int method(
                  int p1,
                  int p2,
                  double somewhatLonger,
                  double d) {
                  log(p1, p2);
                  return p1 + p2;
                }
                """;
        assertEquals(expect, string);
    }


    private static OutputBuilder createExample3() {
        GuideImpl.GuideGenerator gg2 = GuideImpl.generatorForAnnotationList();
        GuideImpl.GuideGenerator gg3 = GuideImpl.generatorForAnnotationList();

        return new OutputBuilderImpl()
                .add(gg2.start())
                .add(new TextImpl("package"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("a.b.c"))
                .add(SymbolEnum.SEMICOLON)

                .add(gg2.mid())
                .add(new TextImpl("import"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("java.util.Set"))
                .add(SymbolEnum.SEMICOLON)

                .add(gg2.mid())
                .add(new TextImpl("import"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("java.util.List"))
                .add(SymbolEnum.SEMICOLON)

                .add(gg2.mid())
                .add(gg3.start())
                .add(SymbolEnum.SINGLE_LINE_COMMENT)
                .add(new TextImpl("this is a comment"))
                .add(SpaceEnum.NEWLINE)

                .add(gg3.mid())
                .add(SymbolEnum.SINGLE_LINE_COMMENT)
                .add(new TextImpl("this is a second comment"))
                .add(SpaceEnum.NEWLINE)

                .add(gg3.mid())
                .add(SymbolEnum.LEFT_BLOCK_COMMENT)
                .add(new TextImpl("block comment"))
                .add(SymbolEnum.RIGHT_BLOCK_COMMENT)

                .add(gg3.mid())
                .add(SymbolEnum.LEFT_BLOCK_COMMENT)
                .add(new TextImpl("block comment"))
                .add(SpaceEnum.NEWLINE)
                .add(new TextImpl("   on a second line"))
                .add(SymbolEnum.RIGHT_BLOCK_COMMENT)

                .add(gg3.mid())
                .add(new TextImpl("record"))
                .add(SpaceEnum.ONE)
                .add(new TextImpl("Record"))
                .add(SymbolEnum.OPEN_CLOSE_PARENTHESIS)
                .add(SymbolEnum.LEFT_BRACE)
                .add(SymbolEnum.RIGHT_BRACE)
                .add(gg3.end())
                .add(gg2.end());
    }

    @Test
    public void test3() {
        OutputBuilder outputBuilder = createExample3();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(80).setSpacesInTab(4).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String out = formatter.write(outputBuilder);
        String expect = """
                package a.b.c;
                import java.util.Set;
                import java.util.List;
                //this is a comment
                //this is a second comment
                /*block comment*/
                /*block comment
                   on a second line*/
                record Record() { }
                """;
        assertEquals(expect, out);
    }

}
