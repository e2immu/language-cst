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

        if (element instanceof Space space) {
            return handleSpace(line, splitInfo, options, space, lastElement);
        }
        if (element instanceof Symbol symbol) {
            return handleSymbol(line, splitInfo, block, options, symbol, lastElement);
        }
        return handleNonSpaceNonSymbol(line, splitInfo, block, options, element);
    }

    private static boolean handleSymbol(Line line,
                                        BlockPrinter.SplitInfo splitInfo,
                                        Formatter2Impl.Block block,
                                        FormattingOptions options,
                                        Symbol symbol,
                                        boolean lastElement) {
        if (!lastElement && !symbol.left().split().isNever()) {
            addSplitPoint(splitInfo, line.length(), symbol.left());
        }
        Line.SpaceLevel left = computeSpaceLevel(options, symbol.left());
        line.mergeSpace(left);
        boolean newLine = line.writeSpace(options.compact(), block.tab() * options.spacesInTab());
        if (newLine && !splitInfo.map().isEmpty()) {
            int pos = line.stringBuilder.lastIndexOf("\n");
            updateForSplit(splitInfo, pos);
        }
        String string = symbol.symbol();
        line.appendNoNewLine(string);
        Line.SpaceLevel right = computeSpaceLevel(options, symbol.right());
        line.setSpace(right);
        if (!lastElement && !symbol.right().split().isNever()) {
            addSplitPoint(splitInfo, line.length(), symbol.right());
        }
        return newLine;
    }

    private static boolean handleSpace(Line line,
                                       BlockPrinter.SplitInfo splitInfo,
                                       FormattingOptions options,
                                       Space space,
                                       boolean lastElement) {
        Line.SpaceLevel spaceLevel = computeSpaceLevel(options, space);
        line.setSpace(spaceLevel);
        if (!lastElement && !space.split().isNever()) {
            addSplitPoint(splitInfo, line.length(), space);
        }
        return space.isNewLine();
    }

    private static Line.SpaceLevel computeSpaceLevel(FormattingOptions options, Space space) {
        Line.SpaceLevel spaceLevel;
        if (space.isNewLine()) {
            spaceLevel = Line.SpaceLevel.NEWLINE;
        } else {
            String minimal = space.minimal();
            String normal = space.write(options);
            if (normal.isEmpty()) {
                spaceLevel = Line.SpaceLevel.NONE;
            } else if (minimal.isEmpty()) {
                spaceLevel = Line.SpaceLevel.SPACE_IS_NICE;
            } else {
                spaceLevel = Line.SpaceLevel.SPACE;
            }
        }
        return spaceLevel;
    }

    private static boolean handleNonSpaceNonSymbol(Line line,
                                                   BlockPrinter.SplitInfo splitInfo,
                                                   Formatter2Impl.Block block,
                                                   FormattingOptions options,
                                                   OutputElement element) {
        line.writeSpace(options.compact(), block.tab() * options.spacesInTab());
        String string = write(element, options, block);
        line.appendBeforeSplit(string);
        boolean multiLine = line.computeAvailable();
        if (line.available() < 0 && !splitInfo.map().isEmpty()) {
            int indent = block.tab() * options.spacesInTab();
            int pos = updateForSplit(splitInfo, indent);
            line.carryOutSplit(pos, indent + options.spacesInTab(), false);
            line.computeAvailable();
            return true;
        }
        if (multiLine && !splitInfo.map().isEmpty()) {
            int pos = line.stringBuilder.lastIndexOf("\n");
            updateForSplit(splitInfo, pos);
        }
        return multiLine;
    }

    private static String write(OutputElement element, FormattingOptions options, Formatter2Impl.Block block) {
        if (element instanceof Text tb && tb.textBlockFormatting() != null) {
            return WriteTextBlock.write(options.spacesInTab() * (block.tab() + 2),
                    tb.minimal(), tb.textBlockFormatting());
        }
        return element.write(options);
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
}
