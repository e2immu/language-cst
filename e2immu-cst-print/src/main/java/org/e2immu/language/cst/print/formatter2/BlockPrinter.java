package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.expression.TextBlock;
import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.element.Space;
import org.e2immu.language.cst.api.output.element.Symbol;
import org.e2immu.language.cst.api.output.element.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BlockPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPrinter.class);

    record SplitInfo(TreeMap<Integer, TreeMap<Integer, Boolean>> map) {
    }

    /**
     * This class's main method returns an Output object.
     * The resulting string has either already been split, or not (extraLines).
     *
     * @param string     already indented according to options and block.tab, on all lines except the first.
     * @param extraLines false = everything fits on one line, string.length == endPos; true = multiple lines
     *                   were needed. all lines except the first have been indented. endPos is computed wrt the last line.
     * @param splitInfo  even if it fits on one theoretical line, we may still want to split. this map returns
     *                   the best positions to split
     */
    record Output(String string, boolean extraLines, SplitInfo splitInfo) {
        int endPos() {
            return extraLines ? Util.charactersUntilNewline(string) : string.length();
        }
    }

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
    the mid positions of the guide. It forwards the 'extraLine' boolean, and computes an 'endPos'.
     */
    Output handleGuideBlock(Formatter2Impl.Block block, FormattingOptions options) {
        SplitInfo splitInfo = new SplitInfo(new TreeMap<>());
        TreeMap<Integer, Boolean> mainSplits = new TreeMap<>();
        splitInfo.map.put(4, mainSplits);
        StringBuilder sb = new StringBuilder();

        boolean extraLine = false;
        Output prevOutput = null;
        for (OutputElement element : block.elements()) {
            if (element instanceof Formatter2Impl.Block sub) {
                Output output = write(sub, options);
                boolean doubleSplit = prevOutput != null && prevOutput.extraLines && output.extraLines;
                int splitPos = bestSplit(sb, sb.length());
                mainSplits.put(splitPos, doubleSplit);
                sb.append(output.string);
                extraLine |= output.extraLines;
                prevOutput = output;
            } else throw new UnsupportedOperationException();
        }

        String string = sb.toString();
        return new Output(string, extraLine, splitInfo);
    }

    /*
    The elements of a normal block are either guide blocks, or non-block (normal) output elements.
     */
    Output handleElements(int maxAvailable,
                          Formatter2Impl.Block block,
                          FormattingOptions options) {
        AtomicBoolean extraLines = new AtomicBoolean();
        SplitInfo splitInfo = new SplitInfo(new TreeMap<>());
        AtomicInteger available = new AtomicInteger(maxAvailable);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int n = block.elements().size();
        boolean protectSpaces = false;
        boolean symmetricalSplit = false;
        for (OutputElement element : block.elements()) {
            if (element instanceof Formatter2Impl.Block sub) {
                boolean split = handleBlock(maxAvailable, available, extraLines, options, sub, sb);
                symmetricalSplit = split && sub.guide().endWithNewLine();
            } else {
                boolean lastElement = i == n - 1;
                handleElement(maxAvailable, available, extraLines, splitInfo, block, options, element, sb, lastElement,
                        protectSpaces, symmetricalSplit);
                if (element.isLeftBlockComment()) protectSpaces = true;
                if (element.isRightBlockComment()) protectSpaces = false;
                symmetricalSplit = false;
            }
            ++i;
        }
        String string = sb.toString();
        return new Output(string, extraLines.get(), splitInfo);
    }

    /*
    The method does not add split points for spaces as the last element.
     */
    void handleElement(int maxAvailable,
                       AtomicInteger available,
                       AtomicBoolean extraLines,
                       SplitInfo splitInfo,
                       Formatter2Impl.Block block,
                       FormattingOptions options,
                       OutputElement element,
                       StringBuilder stringBuilder,
                       boolean lastElement,
                       boolean protectSpaces,
                       boolean symmetricalSplit) {
        Space spaceBefore;
        Space spaceAfter;
        if (element instanceof Space s && !s.isNewLine()) {
            spaceBefore = s;
            spaceAfter = null;
        } else if (element instanceof Symbol symbol) {
            spaceBefore = symbol.left();
            spaceAfter = symbol.right();
        } else {
            spaceBefore = null;
            spaceAfter = null;
        }
        if (spaceBefore != null && !spaceBefore.split().isNever()) {
            int pos = stringBuilder.length();
            while (pos - 1 >= 0 && stringBuilder.charAt(pos - 1) == ' ') --pos;
            addSplitPoint(splitInfo, pos, spaceBefore);
        }
        String string;
        if (element instanceof Text tb && tb.textBlockFormatting() != null) {
            extraLines.set(true);
            string = WriteTextBlock.write(options.spacesInTab() * (block.tab() + 2),
                    tb.minimal(), tb.textBlockFormatting());
        } else {
            if (spaceBefore != null && spaceBefore.split().rank() == 4 && symmetricalSplit) {
                string = "\n" + (" ".repeat(options.spacesInTab() * block.tab())) + element.write(options).stripLeading();
            } else {
                string = element.write(options);
            }
        }
        String string2;
        if (protectSpaces) {
            string2 = string;
        } else if (string.startsWith("\n")) {
            // eat all current spacing, not needed
            available.addAndGet(trim(stringBuilder));
            string2 = string;
        } else {
            // for example, LEFT_BLOCK_COMMENT prints to ' /*'
            string2 = removeLeadingSpacesWhenBuilderEndsInSpace(stringBuilder, string);
        }
        stringBuilder.append(string2);
        if (!lastElement && spaceAfter != null && !spaceAfter.split().isNever()) {
            int pos = stringBuilder.length();
            while (pos - 1 >= 0 && stringBuilder.charAt(pos - 1) == ' ') --pos;
            addSplitPoint(splitInfo, pos, spaceAfter);
        }

        if (string2.endsWith("\n")) {
            // extraLines.set(true);
            splitInfo.map.clear();
            int indent = block.tab() * options.spacesInTab();
            stringBuilder.append(" ".repeat(indent));
            available.set(maxAvailable - indent);
        } else if (string2.contains("\n")) {
            extraLines.set(true);
            available.set(maxAvailable - Util.charactersUntilNewline(string2));
        } else {
            available.addAndGet(-string2.length());
            LOGGER.debug("Appended string '{}' without newlines; available now {}", string2, available);
            if (available.get() < 0 && !splitInfo.map.isEmpty()) {
                LOGGER.debug("We must split: we're over the bound");
                int indent = block.tab() * options.spacesInTab();
                int pos = updateForSplit(splitInfo, indent);
                String insert = "\n" + (" ".repeat(indent));
                // if we've just passed a ' ', then we replace that one
                if(pos < stringBuilder.length()) {
                    char atPos = stringBuilder.charAt(pos);
                    int remainder = stringBuilder.length() - pos;
                    if (atPos == ' ') {
                        stringBuilder.replace(pos, pos + 1, insert);
                    } else {
                        stringBuilder.insert(pos, insert);
                    }
                    available.set(maxAvailable - (remainder + indent));
                    extraLines.set(true);
                }
            }
        }
    }

    private String removeLeadingSpacesWhenBuilderEndsInSpace(StringBuilder sb, String string) {
        if (sb.isEmpty() || Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            return string.stripLeading();
        }
        return string;
    }

    /*
    Add a guide block to the current output.

    The block will be split across the lines indicated by "handleGuideBlock" if it does not fit on the remainder
    of the line, or if it already contains multiple lines.
     */
    boolean handleBlock(int maxAvailable,
                        AtomicInteger available,
                        AtomicBoolean extraLines,
                        FormattingOptions options,
                        Formatter2Impl.Block sub,
                        StringBuilder stringBuilder) {
        Output output = write(sub, options);
        LOGGER.debug("Recursion: received output of block: {}", output);
        int indent = sub.tab() * options.spacesInTab();
        if (output.extraLines || output.endPos() > available.get()) {
            TreeMap<Integer, Boolean> splits = output.splitInfo.map.getOrDefault(4, new TreeMap<>());
            LOGGER.debug("We need to split, and we'll split along all '4' splits: {}; each line will be indented {}",
                    splits, indent);
            String insert = "\n" + (" ".repeat(indent));
            int remainder = 0;

            // first, add
            int start = stringBuilder.length();
            stringBuilder.append(output.string);

            // then, split
            for (Map.Entry<Integer, Boolean> entry : splits.entrySet()) {
                int relativePos = entry.getKey();
                boolean doubleSplit = entry.getValue();
                int pos = relativePos + start;
                if (pos >= 0) {
                    int pos2 = bestSplit(stringBuilder, pos);
                    char atPos = stringBuilder.charAt(pos2);
                    remainder = stringBuilder.length() - pos2;
                    String insertWithDouble = doubleSplit ? "\n" + insert : insert;
                    if (atPos == ' ') {
                        // split is ' \n', and we'll replace at the space by '\n' rather than '\n   '
                        if (pos2 < stringBuilder.length() - 1 && stringBuilder.charAt(pos2 + 1) == '\n') {
                            stringBuilder.replace(pos2, pos2 + 1, doubleSplit ? "\n\n" : "\n");
                        } else {
                            stringBuilder.replace(pos2, pos2 + 1, insertWithDouble);
                        }
                        start += indent;
                    } else if (atPos != '\n') {
                        stringBuilder.insert(pos2, insertWithDouble);
                        start += indent + 1;
                    }
                }
            }
            available.set(maxAvailable - (remainder + indent));
            extraLines.set(true);
            return true;
        }

        if (!stringBuilder.isEmpty() && stringBuilder.charAt(stringBuilder.length() - 1) == '\n') {
            stringBuilder.append(" ".repeat(indent));
            available.set(maxAvailable - indent);
        }
        stringBuilder.append(output.string);
        available.addAndGet(-output.string.length());
        return false;
    }

    private static int bestSplit(StringBuilder sb, int pos) {
        while (pos >= 1 && Character.isWhitespace(sb.charAt(pos - 1))) --pos;
        return pos;
    }

    private static int trim(StringBuilder sb) {
        int cnt = 0;
        while (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
            ++cnt;
        }
        return cnt;
    }

    private int updateForSplit(SplitInfo splitInfo, int indent) {
        int pos = splitInfo.map.lastEntry().getValue().lastKey();
        Iterator<Map.Entry<Integer, TreeMap<Integer, Boolean>>> iterator = splitInfo.map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TreeMap<Integer, Boolean>> entry = iterator.next();
            TreeMap<Integer, Boolean> map = entry.getValue();
            TreeMap<Integer, Boolean> updated = map.keySet().stream()
                    .map(i -> i - pos).filter(i -> i > 0)
                    .map(i -> i + indent)
                    .collect(Collectors.toMap(i -> i, i -> false, (b, c) -> b && c, TreeMap::new));
            if (updated.isEmpty()) {
                iterator.remove();
            } else {
                entry.setValue(updated);
            }
        }
        return pos;
    }

    private void addSplitPoint(SplitInfo splitInfo, int length, Space space) {
        LOGGER.debug("Adding split point in string builder at position {}", length);
        int rank = space.split().rank();
        splitInfo.map.computeIfAbsent(rank, r -> new TreeMap<>()).put(length, false);
    }

}
