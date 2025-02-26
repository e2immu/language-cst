package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.Formatter;
import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.impl.runtime.RuntimeImpl;
import org.e2immu.language.cst.print.FormattingOptionsImpl;
import org.e2immu.language.cst.print.formatter.TestFormatter4;
import org.e2immu.language.cst.print.formatter.TestFormatter5;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test5 {
    private final Runtime runtime = new RuntimeImpl();

    @Test
    public void test0() {
        OutputBuilder outputBuilder = TestFormatter5.createExample0();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(120).setSpacesInTab(4).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String string = formatter.write(outputBuilder);
        String expect = """
                /*line 1 line 2*/ @ImmutableContainer /*IMPLIED*/ @NotNull /*OK*/
                """;
        assertEquals(expect, string);
    }

    @Test
    public void test1() {
        OutputBuilder outputBuilder = TestFormatter5.createExample1();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(120).setSpacesInTab(4).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String string = formatter.write(outputBuilder);
        String expect = """
                /*
                    line 1 is much longer than in the previous example, we want to force everything
                    on multiple lines. So therefore, line 2 is also rather long*/
                    @ImmutableContainer /*IMPLIED*/
                    @NotNull /*OK*/
                """;
        assertEquals(expect, string);
    }

    @Test
    public void test2() {
        OutputBuilder outputBuilder = TestFormatter5.createExample2();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(120).setSpacesInTab(4).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String string = formatter.write(outputBuilder);
        // TODO we'd rather have the space between } and ; gone
        @Language("java")
        String expect = """
                /*
                should raise a warning that the condition is always false, plus that b is never used
                as a consequence, default always returns "c" so we have @NotNull*/
                @ImmutableContainer /*IMPLIED*/
                @NotNull /*OK*/
                public static String method(char c, String b) {
                    return switch(c) { a -> "a"; b -> "b"; default -> c == 'a' || c == 'b' ? b : "c";
                    } ; /*inline conditional evaluates to constant*/
                }
                """;
        assertEquals(expect, string);
    }

    @Test
    public void test2b() {
        OutputBuilder outputBuilder = TestFormatter5.createExample2b();
        FormattingOptions options = new FormattingOptionsImpl.Builder().setLengthOfLine(70).setSpacesInTab(4).build();
        Formatter formatter = new Formatter2Impl(runtime, options);
        String string = formatter.write(outputBuilder);
        @Language("java")
        String expect = """
                package org.e2immu.analyser.parser.conditional.testexample;
                import org.e2immu.annotation.ImmutableContainer;
                import org.e2immu.annotation.NotNull;
                @ImmutableContainer
                public class SwitchExpression_1 {
                
                    /*
                    should raise a warning that the condition is always false, plus that b is never used
                    as a consequence, default always returns "c" so we have @NotNull*/
                    @ImmutableContainer /*IMPLIED*/
                    @NotNull /*OK*/
                    public static String method(char c, String b) {
                        return switch(c) {
                            a -> "a";
                            b -> "b";
                            default -> c == 'a' || c == 'b' ? b : "c";
                        } ; /*inline conditional evaluates to constant*/
                    }
                }
                """;
        assertEquals(expect, string);
    }
}
