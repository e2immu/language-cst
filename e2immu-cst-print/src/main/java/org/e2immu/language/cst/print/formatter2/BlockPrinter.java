package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.FormattingOptions;

public class BlockPrinter {

    record Constraints(int pos) {
    }

    record Output(String string, int extraLines, int startPos, int endPos) {
    }

    Output write(Formatter2Impl.Block block, FormattingOptions options, Constraints constraints) {
        return null;
    }
}
