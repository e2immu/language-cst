package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BlockPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPrinter.class);
    public static final int GUIDE_SPLIT = 4;

    // split level (the higher the better) to position to 'doubleSplit'
    // single split = on new line; double split = leave line empty
    record SplitInfo(TreeMap<Integer, TreeMap<Integer, Boolean>> map) {
    }

    /**
     * This class's main method returns an Output object.
     * The resulting string has either already been split (extraLines), or not (extraLines).
     *
     * @param string       already indented according to options and block.tab, on all lines except the first.
     * @param hasBeenSplit false = everything fits on one line, string.length == endPos; true = multiple lines
     *                     were needed. all lines except the first have been indented. endPos is computed wrt the last line,
     *                     which is probably only needed for continuing fluent lambda chains, text-blocks, etc.
     * @param splitInfo    even if it fits on one theoretical line, we may still want to split. this map returns
     *                     the best positions to split
     */
    record Output(String string, boolean hasBeenSplit, SplitInfo splitInfo) {
        int endPos() {
            return hasBeenSplit ? Util.charactersUntilAndExcludingLastNewline(string) : string.length();
        }
    }

    /**
     * main entry point, and point of recursion.
     * When called as the main entry point on a primary type, the splitInfo can be ignored, and only the
     * final space needs converting into a newline.
     *
     * @param block   the block, as prepared in Formatter2Impl
     * @param options overall formatting options, mostly to determine line length, tab spaces
     * @return an output instance
     */
    Output write(Formatter2Impl.Block block, FormattingOptions options) {
        int maxAvailable = options.lengthOfLine() - block.tab() * options.spacesInTab();
        if (block.guide() != null) {
            return handleGuideBlock(block, options);
        }
        return handleElements(maxAvailable, block, options);
    }

    /*
    The elements of a guide block are blocks themselves.
    This method does not carry out line splitting, it simply marks possible split positions at the beginning, and at
    the mid positions of the guide. It forwards the 'hasBeenSplit' boolean, and computes an 'endPos'.

    All earlier splits are discarded.

    The doubleSplit is part of our own style feeling: unless caused by comments, we want a blank line
    by lines that have internally been split.

    prevOutput helps in the computation of doubleSplit.
     */
    Output handleGuideBlock(Formatter2Impl.Block block, FormattingOptions options) {
        SplitInfo splitInfo = new SplitInfo(new TreeMap<>());
        TreeMap<Integer, Boolean> guideSplits = new TreeMap<>();
        splitInfo.map.put(GUIDE_SPLIT, guideSplits);
        StringBuilder sb = new StringBuilder();
        boolean hasBeenSplit = false;
        Output prevOutput = null;
        for (OutputElement element : block.elements()) {
            if (element instanceof Formatter2Impl.Block sub) {
                Output output = write(sub, options);
                boolean doubleSplit = prevOutput != null && prevOutput.hasBeenSplit && output.hasBeenSplit;
                guideSplits.put(sb.length(), doubleSplit);
                sb.append(output.string);
                hasBeenSplit |= output.hasBeenSplit;
                prevOutput = output;
            } else throw new UnsupportedOperationException();
        }
        return new Output(sb.toString(), hasBeenSplit, splitInfo);
    }

    /*
    The elements of a normal block are either guide blocks, or non-block (normal) output elements.

    protectSpaces is solely concerned with handling of block comments. Inside block comments, we keep the
    original spaces.
     */
    Output handleElements(int maxAvailable,
                          Formatter2Impl.Block block,
                          FormattingOptions options) {
        boolean hasBeenSplit = false;
        SplitInfo splitInfo = new SplitInfo(new TreeMap<>());
        Line line = new Line(maxAvailable);
        int i = 0;
        int n = block.elements().size();
        boolean protectSpaces = false;
        boolean symmetricalSplit = false;
        for (OutputElement element : block.elements()) {
            if (element instanceof Formatter2Impl.Block sub) {
                boolean blockHasBeenSplit = handleBlock(line, options, sub);
                hasBeenSplit |= blockHasBeenSplit;
                symmetricalSplit = blockHasBeenSplit && sub.guide().endWithNewLine();
            } else {
                boolean lastElement = i == n - 1;
                hasBeenSplit |= ElementPrinter.handleElement(line, splitInfo, block, options,
                        element, lastElement, protectSpaces, symmetricalSplit);
                if (element.isLeftBlockComment()) protectSpaces = true;
                if (element.isRightBlockComment()) protectSpaces = false;
                symmetricalSplit = false;
            }
            ++i;
        }
        String string = line.toString();
        return new Output(string, hasBeenSplit, splitInfo);
    }

    /*
    Add a guide block to the current output.

    The block will be split across the lines indicated by "handleGuideBlock" if it does not fit on the remainder
    of the line, or if it already contains multiple lines.
     */
    boolean handleBlock(Line line, FormattingOptions options, Formatter2Impl.Block sub) {
        Output output = write(sub, options);
        LOGGER.debug("Result of recursion: received output of block: {}", output);

        int indent = sub.tab() * options.spacesInTab();
        if (output.hasBeenSplit || output.endPos() > line.available()) {
            splitOutputOfBlock(line, output, indent);
            return true;
        }
        // no newline, within bounds -> no splitting required. we simply append
        if (line.isNotEmptyDoesNotEndInNewLine()) {
            line.appendNoNewLine(" ".repeat(indent));
        }
        line.appendNoNewLine(output.string);
        return false;
    }

    private static void splitOutputOfBlock(Line line, Output output, int indent) {
        TreeMap<Integer, Boolean> splits = output.splitInfo.map.getOrDefault(GUIDE_SPLIT, new TreeMap<>());
        LOGGER.debug("We need to split, and we'll split along all '{}' splits: {}; each line will be indented {}",
                GUIDE_SPLIT, splits, indent);
        boolean allowSplitAtPosition0 = line.isNotEmptyDoesNotEndInNewLine();

        int start = line.length();
        line.appendBeforeSplit(output.string);
        for (Map.Entry<Integer, Boolean> entry : splits.entrySet()) {
            int relativePos = entry.getKey();
            int pos = relativePos + start;
            assert pos < line.length();
            if (allowSplitAtPosition0 || pos > 0) {
                boolean doubleSplit = entry.getValue();
                start += line.carryOutSplit(pos, indent, doubleSplit);
            } // else: we never split at the beginning of the line, there must be something already
        }
        line.computeAvailable();
    }
}
