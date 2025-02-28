package org.e2immu.language.cst.print.formatter2;

import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.element.Space;
import org.e2immu.language.cst.api.output.element.Symbol;
import org.e2immu.language.cst.api.output.element.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ElementPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementPrinter.class);

    /*
    The method does not add split points for spaces as the last element.
     */
    static boolean handleElement(Line line,
                                 BlockPrinter.SplitInfo splitInfo,
                                 Formatter2Impl.Block block,
                                 FormattingOptions options,
                                 OutputElement element,
                                 boolean lastElement,
                                 boolean protectSpaces,
                                 boolean symmetricalSplit) {

        boolean hasBeenSplit = false;
        Space spaceBefore;
        Space spaceAfter;
        if (element instanceof Space s && !s.isNewLine()) {
            spaceBefore = s;
            spaceAfter = null;
        } else if (element instanceof Symbol symbol) {
            spaceBefore = symbol.left();
            spaceAfter = symbol.right();
        } else {
            
        }

        // there are a number of cases that we want to
        // add split point before
        if (spaceBefore != null && !spaceBefore.split().isNever()) {
            int pos = line.length();
            while (pos - 1 >= 0 && line.lastCharacter() == ' ') --pos; // TODO this is the same code as bestPos(...)
            addSplitPoint(splitInfo, pos, spaceBefore);
        }

        String string = write(element, options, block);

        //   if (spaceBefore != null && spaceBefore.split().rank() == BlockPrinter.GUIDE_SPLIT && symmetricalSplit) {
        //                string = "\n" + (" ".repeat(options.spacesInTab() * block.tab())) + output.stripLeading();

        if (!protectSpaces && (string.startsWith("\n") || string.startsWith(" "))) {
            // eat all current spacing, not needed
            line.trim();
        }
        // FIXME append point
        if (!lastElement && spaceAfter != null && !spaceAfter.split().isNever()) {
            int pos = stringBuilder.length();
            while (pos - 1 >= 0 && stringBuilder.charAt(pos - 1) == ' ') --pos;
            addSplitPoint(splitInfo, pos, spaceAfter);
        }

        if (string2.endsWith("\n")) {
            // extraLines.set(true);
            splitInfo.map().clear();
            int indent = block.tab() * options.spacesInTab();
            stringBuilder.append(" ".repeat(indent));
            line.newAvailable(indent);
            available.set(maxAvailable - indent);
        } else if (string2.contains("\n")) {
            hasBeenSplit = true;
            available.set(maxAvailable - Util.charactersUntilAndExcludingLastNewline(string2));
        } else {
            available.addAndGet(-string2.length());
            LOGGER.debug("Appended string '{}' without newlines; available now {}", string2, available);
            if (available.get() < 0 && !splitInfo.map().isEmpty()) {
                LOGGER.debug("We must split: we're over the bound");
                int indent = block.tab() * options.spacesInTab();
                int pos = updateForSplit(splitInfo, indent);
                String insert = "\n" + (" ".repeat(indent));
                // if we've just passed a ' ', then we replace that one
                if (pos < stringBuilder.length()) {
                    char atPos = stringBuilder.charAt(pos);
                    int remainder = stringBuilder.length() - pos;
                    if (atPos == ' ') {
                        stringBuilder.replace(pos, pos + 1, insert);
                    } else {
                        stringBuilder.insert(pos, insert);
                    }
                    available.set(maxAvailable - (remainder + indent));
                    hasBeenSplit = true;
                }
            }
        }
        return hasBeenSplit;
    }

    private static String write(OutputElement element, FormattingOptions options, Formatter2Impl.Block block) {
        if (element instanceof Text tb && tb.textBlockFormatting() != null) {
           return  WriteTextBlock.write(options.spacesInTab() * (block.tab() + 2),
                    tb.minimal(), tb.textBlockFormatting());
        }
        return element.write(options);
    }


    private static int trim(StringBuilder sb) {
        int cnt = 0;
        while (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
            ++cnt;
        }
        return cnt;
    }

    private static int updateForSplit(BlockPrinter.SplitInfo splitInfo, int indent) {
        int pos = splitInfo.map().lastEntry().getValue().lastKey();
        Iterator<Map.Entry<Integer, TreeMap<Integer, Boolean>>> iterator = splitInfo.map().entrySet().iterator();
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

    static void addSplitPoint(BlockPrinter.SplitInfo splitInfo, int length, Space space) {
        LOGGER.debug("Adding split point in string builder at position {}", length);
        int rank = space.split().rank();
        splitInfo.map().computeIfAbsent(rank, r -> new TreeMap<>()).put(length, false);
    }

    static String removeLeadingSpacesWhenBuilderEndsInSpace(StringBuilder sb, String string) {
        if (sb.isEmpty() || Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            return string.stripLeading();
        }
        return string;
    }

}
