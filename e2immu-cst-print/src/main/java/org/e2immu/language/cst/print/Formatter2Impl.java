package org.e2immu.language.cst.print;

import org.e2immu.language.cst.api.output.Formatter;
import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.element.Guide;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.print.formatter2.Util;

import java.util.*;

public record Formatter2Impl(Runtime runtime, FormattingOptions options) implements Formatter {
    @Override
    public String write(OutputBuilder outputBuilder) {
        List<OutputElement> list = options.skipComments() ? Util.removeComments(outputBuilder.list())
                : outputBuilder.list();
        Iterator<OutputElement> iterator = list.iterator();
        R r = collectElements(iterator, 0);
        return r.block == null ? "" : r.block.write(options);
    }

    // for testing
    public String minimal(OutputBuilder outputBuilder) {
        Iterator<OutputElement> iterator = outputBuilder.list().iterator();
        R r = collectElements(iterator, 0);
        return r.block == null ? "" : r.block.minimal();
    }

    record Block(int tab, List<OutputElement> elements, Guide guide) implements OutputElement {
        Block(int tab, List<OutputElement> elements, Guide guide) {
            this.tab = tab;
            this.elements = Objects.requireNonNull(elements);
            assert !elements.isEmpty();
            this.guide = guide;
        }

        @Override
        public String minimal() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            indent(sb);
            for (OutputElement element : elements) {
                if (element.isNewLine()) {
                    sb.append('\n');
                    indent(sb);
                } else {
                    String minimal = element.minimal();
                    sb.append(minimal);
                }
            }
            return sb.toString();
        }

        private void indent(StringBuilder sb) {
            sb.append(" ".repeat(tab * 2));
        }

        @Override
        public String generateJavaForDebugging() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String write(FormattingOptions options) {
            return "";
        }
    }

    private record R(boolean mid, Block block) {
    }

    private R collectElements(Iterator<OutputElement> iterator, int tab) {
        List<OutputElement> elements = new ArrayList<>();
        Guide endGuide = null;
        while (iterator.hasNext()) {
            OutputElement element = iterator.next();
            if (element instanceof Guide g) {
                if (g.positionIsStart()) {
                    Block block = parseBlock(iterator, tab, g);
                    if (block != null) {
                        elements.add(block);
                    }
                } else {
                    endGuide = g;
                    break;
                }
            } else {
                elements.add(element);
            }
        }
        boolean mid = endGuide != null && endGuide.positionIsMid();
        if (!elements.isEmpty()) {
            return new R(mid, new Block(tab, List.copyOf(elements), null));
        }
        return new R(mid, null);
    }

    private Block parseBlock(Iterator<OutputElement> iterator, int tab, Guide g) {
        List<OutputElement> subBlocks = new ArrayList<>();
        while (true) {
            R r = collectElements(iterator, tab + 1);
            if (r.block != null) {
                subBlocks.add(r.block);
            }
            if (!r.mid) {
                break;
            }
        }
        if (subBlocks.isEmpty()) return null;
        return new Block(tab, subBlocks, g);
    }
}
