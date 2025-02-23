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
        Block block = collectElements(iterator, 0, null);
        if (block == null) return "";
        return block.write(options);
    }

    // for testing
    public String minimal(OutputBuilder outputBuilder) {
        Iterator<OutputElement> iterator = outputBuilder.list().iterator();
        Block block = collectElements(iterator, 0, null);
        return block == null ? "" : block.minimal();
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
            if (guide != null && guide.startWithNewLine()) sb.append("\n");
            for (int i = 0; i < elements.size(); ++i) {
                sb.append(" ".repeat(tab * 2));
                String minimal = elements.get(i).minimal();
                sb.append(minimal);
                if (i != elements.size() - 1) sb.append("\n");
            }
            if (guide != null && guide.endWithNewLine()) sb.append("\n");
            return sb.toString();
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

    private Block collectElements(Iterator<OutputElement> iterator, int tab, Guide guide) {
        List<OutputElement> elements = new ArrayList<>();
        while (iterator.hasNext()) {
            OutputElement element = iterator.next();
            if (element instanceof Guide g) {
                if (g.positionIsStart() || g.positionIsMid()) {
                    Block sub = collectElements(iterator, tab + 1, g);
                    if (sub != null) {
                        elements.add(sub);
                    }
                } // else: end; we'll ignore this one
            } else {
                elements.add(element);
            }
        }
        if (elements.isEmpty()) {
            return null;
        }
        return new Block(tab, List.copyOf(elements), guide);
    }
}
