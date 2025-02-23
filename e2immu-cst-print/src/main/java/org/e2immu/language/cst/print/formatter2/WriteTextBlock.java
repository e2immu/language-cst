package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.element.TextBlockFormatting;

import java.io.IOException;
import java.io.Writer;

import static org.e2immu.language.cst.print.formatter2.Util.indent;

public class WriteTextBlock {
    public static void write(Writer w, int indentIn, String string, TextBlockFormatting textBlockFormatting) throws IOException {
        w.append("\"\"\"\n");
        int indent = textBlockFormatting.optOutWhiteSpaceStripping() ? 0 : indentIn;
        indent(indent, w);
        char[] chars = string.toCharArray();
        int i = 0;
        while (i < chars.length) {
            char c = chars[i];
            if (textBlockFormatting.lineBreaks().contains(i)) {
                w.append('\\');
                w.append('\n');
                indent(indent, w);
                w.append(c);
            } else if (c == '\n') {
                w.append('\n');
                indent(indent, w);
            } else {
                w.append(c);
            }
            ++i;
        }
        if (!textBlockFormatting.trailingClosingQuotes()) {
            w.append("\n");
            indent(indent, w);
        }
        w.append("\"\"\"");
    }
}
