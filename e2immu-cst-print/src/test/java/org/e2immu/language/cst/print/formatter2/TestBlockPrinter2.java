package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.Formatter;
import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.runtime.Runtime;
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
        String string  = formatter.write(outputBuilder);
        String formatted = "public int method(int p1, int p2) { return p1 + p2; }\n";
        assertEquals(formatted, string);
    }

    @Test
    public void test1b() {
        OutputBuilder outputBuilder = TestFormatter1.createExample1();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(30).setSpacesInTab(4).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String string  = formatter.write(outputBuilder);
        // TODO ( prev line, } to new line
        String expect = """
                public int method
                    (int p1,
                    int p2) {
                    return p1 + p2; }
                """;
        assertEquals(expect, string);
    }

    @Test
    public void test2a() {
        OutputBuilder outputBuilder = TestFormatter1.createExample2();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(20).setSpacesInTab(2).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String string  = formatter.write(outputBuilder);
        String expect = """
                public int method
                  (int p1,
                  int p2,
                  double somewhatLonger,
                  double d) {
                  log(p1, p2);
                  return p1 + p2;
                }
                """;
        assertEquals(expect, string);
    }
}
