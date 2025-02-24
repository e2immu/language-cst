package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.element.Space;
import org.e2immu.language.cst.api.output.element.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPrinter.class);

    /**
     * This class's main method returns an Output object.
     * The resulting string has either already been split, or not (extraLines).
     *
     * @param string         already indented according to options and block.tab
     * @param extraLines     false = everything fits on one line, string.length == endPos
     * @param endPos         when multiple lines, this one gives the length of the last line if it makes sense to
     *                       continue there (e.g. closing """ of a text block, closing ')'). It equals 0 when a line has
     *                       been completed (e.g., end of statement, closing '}').
     * @param possibleSplits even if it fits on one theoretical line, we may still want to split. this map returns
     *                       the best positions to split
     */
    record Output(String string, boolean extraLines, int endPos, TreeMap<Integer, Set<Integer>> possibleSplits) {
    }

    Output write(Formatter2Impl.Block block, FormattingOptions options) {
        AtomicBoolean extraLines = new AtomicBoolean();
        TreeMap<Integer, Set<Integer>> possibleSplits = new TreeMap<>();
        int available = options.lengthOfLine() - block.tab() * options.spacesInTab();

        String string;
        if (block.guide() != null) {
            string = handleGuideBlock(available, extraLines, possibleSplits, block, options);
        } else {
            string = handleElements(available, extraLines, possibleSplits, block, options);
        }
        int endPos;
        if (extraLines.get()) {
            endPos = Util.charactersUntilNewline(string);
        } else {
            endPos = string.length();
        }
        return new Output(string, extraLines.get(), endPos, possibleSplits);
    }

    private String handleElements(int availableIn,
                                  AtomicBoolean extraLines,
                                  TreeMap<Integer, Set<Integer>> possibleSplits,
                                  Formatter2Impl.Block block,
                                  FormattingOptions options) {
        int available = availableIn;
        StringBuilder stringBuilder = new StringBuilder();
        for (OutputElement element : block.elements()) {
            if (element instanceof Formatter2Impl.Block sub) {
                Output output = write(sub, options);
                LOGGER.debug("Recursion: received output of block: {}", output);

            } else {
                Space spaceBefore;
                Space space;
                if (element instanceof Space s) {
                    space = s;
                    spaceBefore = null;
                } else if (element instanceof Symbol symbol) {
                    space = symbol.right();
                    spaceBefore = symbol.left();
                } else {
                    space = null;
                    spaceBefore = null;
                }
                if (spaceBefore != null && !spaceBefore.split().isNever()) {
                    addSplitPoint(possibleSplits, stringBuilder.length(), spaceBefore);
                }
                String string = element.write(options);
                stringBuilder.append(string);
                if (space != null && !space.split().isNever()) {
                    addSplitPoint(possibleSplits, stringBuilder.length(), space);
                }

                if (string.endsWith("\n")) {
                    extraLines.set(true);
                    available = availableIn;
                } else if (string.contains("\n")) {
                    extraLines.set(true);
                    available = availableIn - Util.charactersUntilNewline(string);
                } else {
                    available -= string.length();
                    LOGGER.debug("Appended string '{}' without newlines; available now {}", string, available);
                    if (available < 0) {
                        LOGGER.debug("We must split: we're oven the bound");
                    }
                }
            }
        }
        return stringBuilder.toString();
    }

    private void addSplitPoint(TreeMap<Integer, Set<Integer>> possibleSplits, int length, Space spaceBefore) {
        LOGGER.debug("Adding split point in string builder at positio {}", length);
    }

    private String handleGuideBlock(int available,
                                    AtomicBoolean extraLines,
                                    TreeMap<Integer, Set<Integer>> possibleSplits,
                                    Formatter2Impl.Block block,
                                    FormattingOptions options) {
        throw new UnsupportedOperationException();
    }
}
