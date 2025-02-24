package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.element.Space;
import org.e2immu.language.cst.api.output.element.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BlockPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPrinter.class);

    /**
     * This class's main method returns an Output object.
     * The resulting string has either already been split, or not (extraLines).
     *
     * @param string         already indented according to options and block.tab, on all lines except the first.
     * @param extraLines     false = everything fits on one line, string.length == endPos; true = multiple lines
     *                       were needed. all lines except the first have been indented. endPos is computed wrt the last line.
     * @param possibleSplits even if it fits on one theoretical line, we may still want to split. this map returns
     *                       the best positions to split
     */
    record Output(String string, boolean extraLines, TreeMap<Integer, TreeSet<Integer>> possibleSplits) {
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
        TreeMap<Integer, TreeSet<Integer>> possibleSplits = new TreeMap<>();
        TreeSet<Integer> mainSplits = new TreeSet<>();
        possibleSplits.put(4, mainSplits);
        StringBuilder sb = new StringBuilder();

        boolean extraLine = false;
        for (OutputElement element : block.elements()) {
            if (element instanceof Formatter2Impl.Block sub) {
                Output output = write(sub, options);
                mainSplits.add(sb.length() - 1);
                sb.append(output.string);
                extraLine |= output.extraLines;
            } else throw new UnsupportedOperationException();
        }

        String string = sb.toString();
        return new Output(string, extraLine, possibleSplits);
    }

    /*
    The elements of a normal block are either guide blocks, or non-block (normal) output elements.
     */
    Output handleElements(int maxAvailable,
                          Formatter2Impl.Block block,
                          FormattingOptions options) {
        AtomicBoolean extraLines = new AtomicBoolean();
        TreeMap<Integer, TreeSet<Integer>> possibleSplits = new TreeMap<>();
        AtomicInteger available = new AtomicInteger(maxAvailable);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int n = block.elements().size();
        for (OutputElement element : block.elements()) {
            if (element instanceof Formatter2Impl.Block sub) {
                handleBlock(maxAvailable, available, extraLines, options, sub, sb);
            } else {
                boolean lastElement = i == n - 1;
                handleElement(maxAvailable, available, extraLines, possibleSplits, block, options, element, sb, lastElement);
            }
            ++i;
        }
        String string = sb.toString();
        return new Output(string, extraLines.get(), possibleSplits);
    }

    /*
    The method does not add split points for spaces as the last element.
     */
    void handleElement(int maxAvailable,
                       AtomicInteger available,
                       AtomicBoolean extraLines,
                       TreeMap<Integer, TreeSet<Integer>> possibleSplits,
                       Formatter2Impl.Block block,
                       FormattingOptions options,
                       OutputElement element,
                       StringBuilder stringBuilder,
                       boolean lastElement) {
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
            addSplitPoint(possibleSplits, pos, spaceBefore);
        }
        String string = element.write(options);
        if (string.startsWith(" ") || string.startsWith("\n")) available.addAndGet(trim(stringBuilder));
        stringBuilder.append(string);
        if (!lastElement && spaceAfter != null && !spaceAfter.split().isNever()) {
            int pos = stringBuilder.length();
            while (pos - 1 >= 0 && stringBuilder.charAt(pos - 1) == ' ') --pos;
            addSplitPoint(possibleSplits, pos, spaceAfter);
        }

        if (string.endsWith("\n")) {
            extraLines.set(true);
            possibleSplits.clear();
            int indent = block.tab() * options.spacesInTab();
            stringBuilder.append(" ".repeat(indent));
            available.set(maxAvailable - indent);
        } else if (string.contains("\n")) {
            extraLines.set(true);
            available.set(maxAvailable - Util.charactersUntilNewline(string));
        } else {
            available.addAndGet(-string.length());
            LOGGER.debug("Appended string '{}' without newlines; available now {}", string, available);
            if (available.get() < 0 && !possibleSplits.isEmpty()) {
                LOGGER.debug("We must split: we're over the bound");
                int indent = block.tab() * options.spacesInTab();
                int pos = updateForSplit(possibleSplits, indent);
                String insert = "\n" + (" ".repeat(indent));
                // if we've just passed a ' ', then we replace that one
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

    /*
    Add a guide block to the current output.

    The block will be split across the lines indicated by "handleGuideBlock" if it does not fit on the remainder
    of the line, or if it already contains multiple lines.
     */
    void handleBlock(int maxAvailable,
                     AtomicInteger available,
                     AtomicBoolean extraLines,
                     FormattingOptions options,
                     Formatter2Impl.Block sub,
                     StringBuilder stringBuilder) {
        Output output = write(sub, options);
        LOGGER.debug("Recursion: received output of block: {}", output);
        int indent = sub.tab() * options.spacesInTab();
        if (output.extraLines || output.endPos() > available.get()) {
            TreeSet<Integer> splits = output.possibleSplits.getOrDefault(4, new TreeSet<>());
            LOGGER.debug("We need to split, and we'll split along all '4' splits: {}; each line will be indented {}",
                    splits, indent);
            String insert = "\n" + (" ".repeat(indent));
            int remainder = 0;

            // first, add
            int start = stringBuilder.length();
            stringBuilder.append(output.string);

            // then, split
            for (int relativePos : splits) {
                int pos = relativePos + start;
                if (pos >= 0) {
                    char atPos = stringBuilder.charAt(pos);
                    remainder = stringBuilder.length() - pos;
                    if (atPos == ' ') {
                        stringBuilder.replace(pos, pos + 1, insert);
                        start += indent;
                    } else if(atPos != '\n'){
                        stringBuilder.insert(pos, insert);
                        start += indent + 1;
                    }
                }
            }
            available.set(maxAvailable - (remainder + indent));
            extraLines.set(true);
        } else {
            if (!stringBuilder.isEmpty() && stringBuilder.charAt(stringBuilder.length() - 1) == '\n') {
                stringBuilder.append(" ".repeat(indent));
                available.set(maxAvailable - indent);
            }
            stringBuilder.append(output.string);
            available.addAndGet(-output.string.length());
        }
    }

    private static int trim(StringBuilder sb) {
        int cnt = 0;
        while (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
            ++cnt;
        }
        return cnt;
    }

    private int updateForSplit(TreeMap<Integer, TreeSet<Integer>> possibleSplits, int indent) {
        int pos = possibleSplits.lastEntry().getValue().last();
        Iterator<Map.Entry<Integer, TreeSet<Integer>>> iterator = possibleSplits.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TreeSet<Integer>> entry = iterator.next();
            TreeSet<Integer> updated = entry.getValue().stream().map(i -> i - pos).filter(i -> i > 0)
                    .map(i -> i + indent).collect(Collectors.toCollection(TreeSet::new));
            if (updated.isEmpty()) {
                iterator.remove();
            } else {
                entry.setValue(updated);
            }
        }
        return pos;
    }

    private void addSplitPoint(TreeMap<Integer, TreeSet<Integer>> possibleSplits, int length, Space space) {
        LOGGER.debug("Adding split point in string builder at position {}", length);
        int rank = space.split().rank();
        possibleSplits.computeIfAbsent(rank, r -> new TreeSet<>()).add(length);
    }

}
