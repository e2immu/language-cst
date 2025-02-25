package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.OutputElement;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static int charactersUntilNewline(String string) {
        int nl = string.lastIndexOf('\n');
        if (nl < 0) throw new UnsupportedOperationException();
        return string.length() - (nl + 1);
    }

    public static void indent(int spaces, Writer writer) throws IOException {
        for (int i = 0; i < spaces; i++) writer.write(" ");
    }

    public static List<OutputElement> removeComments(List<OutputElement> list) {
        List<OutputElement> elements = new ArrayList<>(list.size());
        int commentDepth = 0;
        boolean dropUntilNewline = false;
        for (OutputElement outputElement : list) {
            if (outputElement.isLeftBlockComment()) {
                commentDepth++;
            } else if (outputElement.isRightBlockComment()) {
                commentDepth--;
            } else if (commentDepth == 0) {
                if (outputElement.isSingleLineComment()) {
                    dropUntilNewline = true;
                } else if (outputElement.isNewLine()) {
                    dropUntilNewline = false;
                }
                if (!dropUntilNewline) {
                    elements.add(outputElement);
                }
            }
        }
        return elements;
    }
}
