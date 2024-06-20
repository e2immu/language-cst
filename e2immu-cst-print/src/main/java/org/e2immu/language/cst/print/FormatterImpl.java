package org.e2immu.language.cst.print;

import org.e2immu.language.cst.api.output.Formatter;
import org.e2immu.language.cst.api.output.FormattingOptions;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.element.Guide;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.print.formatter.CurrentExceeds;
import org.e2immu.language.cst.print.formatter.Forward;
import org.e2immu.language.cst.print.formatter.ForwardInfo;
import org.e2immu.language.cst.print.formatter.Lookahead;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public record FormatterImpl(Runtime runtime, FormattingOptions options) implements Formatter {

    public String write(OutputBuilder outputBuilder) {
        try (StringWriter stringWriter = new StringWriter()) {
            write(outputBuilder, stringWriter);
            return stringWriter.toString();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static final int NO_GUIDE = -1;
    private static final int LINE_SPLIT = -2;

    // guideIndex -1 is used for no tabs at all
    // guideIndex == -2 is for line split without guides
    // endWithNewline forces a newline at the end of the guide
    private static class Tab {
        final int indent;
        final int guideIndex;
        final boolean allowNewLineBefore;
        int countLines;
        boolean seenFirstMid;
        Writer writer = new StringWriter();
        boolean previousWriteNewLineBefore;

        Tab(int indent, int guideIndex, boolean allowNewLineBefore) {
            this.indent = indent;
            this.guideIndex = guideIndex;
            this.allowNewLineBefore = allowNewLineBefore;
        }

        void increment() {
            if (seenFirstMid) countLines++;
        }

        @Override
        public String toString() {
            return "Tab indent " + indent + " guide index " + guideIndex + " newLinesBefore? " + allowNewLineBefore;
        }
    }

    // guides typically organised as  ( S int i, M int j, M int k E )

    public void write(OutputBuilder outputBuilder, Writer writer) throws IOException {
        List<OutputElement> list = optionallyRemoveComments(outputBuilder.list());
        Stack<Tab> tabs = new Stack<>();
        int pos = 0;
        int end = list.size();
        boolean writeNewLine = true; // only indent when a newline was written
        while (pos < end) {

            int indent = tabs.isEmpty() ? 0 : tabs.peek().indent;

            // we should only indent if we wrote a new line
            if (writeNewLine) indent(indent, writer(writer, tabs));
            int lineLength = options.lengthOfLine() - indent;
            CurrentExceeds currentExceeds = Lookahead.lookAhead(runtime, options, list, pos, lineLength);

            NewLineDouble newLineDouble = NOT_END;
            boolean lineSplit = LINE_SPLIT == (tabs.isEmpty() ? NO_GUIDE : tabs.peek().guideIndex);
            if (currentExceeds.exceeds() != null) {
                pos = handleExceeds(currentExceeds, lineSplit, tabs, list, writer(writer, tabs), pos);
                writeNewLine = true;
            } else if (currentExceeds.current() == null) {
                // direct newline hit
                writeNewLine = false;
                pos++;
            } else {
                writeLine(list, writer(writer, tabs), pos, currentExceeds.current().pos());
                if (lineSplit) {
                    pop(tabs, "", writer);
                }
                pos = currentExceeds.current().pos() + 1; // move one step beyond

                // tab management: note that exceeds is never a guide.
                Guide guide = currentExceeds.current().guide();
                if (guide != null) {
                    newLineDouble = handleGuide(guide, tabs, writer);
                    writeNewLine = newLineDouble.writeNewLine;
                } else {
                    writeNewLine = true;
                }
            }
            if (writeNewLine) {
                tabs.forEach(Tab::increment);
                writer(writer, tabs).write("\n");
            }
            if (newLineDouble.pop) {
                pop(tabs, !tabs.isEmpty() && tabs.peek().previousWriteNewLineBefore ? "\n" : "", writer);
            } else if (newLineDouble.swapWriter && !tabs.isEmpty()) {
                Tab tab = tabs.peek();
                assert tab != null;
                swap(tabs, newLineDouble.writeNewLineBefore || tab.previousWriteNewLineBefore ? "\n" : "", writer);
                tab.previousWriteNewLineBefore = newLineDouble.writeNewLineBefore;
            }
        }
        while (!tabs.isEmpty()) pop(tabs, "", writer);
        if (!writeNewLine) writer.write("\n"); // end on a newline
    }

    private List<OutputElement> optionallyRemoveComments(List<OutputElement> list) {
        if (options.skipComments()) {
            List<OutputElement> elements = new ArrayList<>(list.size());
            int commentDepth = 0;
            for (OutputElement outputElement : list) {
                if (outputElement.isLeftBlockComment()) {
                    commentDepth++;
                } else if (outputElement.isRightBlockComment()) {
                    commentDepth--;
                } else if (commentDepth == 0) {
                    elements.add(outputElement);
                }
            }
            return elements;
        }
        return new ArrayList<>(list);
    }

    private static void pop(Stack<Tab> tabs, String writeBefore, Writer writer) throws IOException {
        if (!tabs.isEmpty()) {
            Tab tab = tabs.pop();
            Writer dest = writer(writer, tabs);
            dest.write(writeBefore);
            dest.write(tab.writer.toString());
        }
    }

    private static void swap(Stack<Tab> tabs, String writeBefore, Writer writer) throws IOException {
        Writer destination = tabs.size() == 1 ? writer : tabs.get(tabs.size() - 2).writer;
        destination.write(writeBefore);
        Tab tab = tabs.peek();
        destination.write(tab.writer.toString());
        tab.writer = new StringWriter();
    }

    private static Writer writer(Writer writer, Stack<Tab> tabs) {
        return tabs.isEmpty() ? writer : tabs.peek().writer;
    }

    private int handleExceeds(CurrentExceeds lookAhead, boolean lineSplit, Stack<Tab> tabs,
                              List<OutputElement> list, Writer writer, int pos) throws IOException {
        if (!lineSplit) {
            int previousIndent = tabs.isEmpty() ? 0 : tabs.peek().indent;
            tabs.add(new Tab(previousIndent + options.tabsForLineSplit() * options.spacesInTab(),
                    LINE_SPLIT, false));
        }
        ForwardInfo exceeds9 = lookAhead.exceeds();
        assert exceeds9 != null;
        if (lookAhead.current() != null) {
            writeLine(list, writer, pos, lookAhead.current().pos());
            return exceeds9.pos();
        } else {
            writeLine(list, writer, pos, exceeds9.pos());
            return exceeds9.pos() + 1;
        }
    }

    private static final NewLineDouble NOT_END = new NewLineDouble(false, false, false, false);

    record NewLineDouble(boolean writeNewLine, boolean writeNewLineBefore, boolean swapWriter, boolean pop) {
    }

    private NewLineDouble handleGuide(Guide guide, Stack<Tab> tabs, Writer writer) throws IOException {
        if (guide.positionIsStart()) {
            int previousIndent = tabs.isEmpty() ? 0 : tabs.peek().indent;
            tabs.add(new Tab(previousIndent + guide.tabs() * options.spacesInTab(),
                    guide.index(), guide.allowNewLineBefore()));
            // do we need a newline? in the case of ending on a start after {, (, we do
            // in the case of the start of an annotation sequence, we don't
            return new NewLineDouble(guide.startWithNewLine(), false, false, false);
        }

        // MID or END
        while (!tabs.isEmpty() && tabs.peek().guideIndex == LINE_SPLIT) {
            pop(tabs, "", writer);
        }
        assert tabs.isEmpty() || tabs.peek().guideIndex == guide.index() :
                "Tabs: " + tabs + "; guide.index " + guide.index();

        boolean writeNewLineBefore = !tabs.isEmpty() && tabs.peek().countLines > 1 && tabs.peek().allowNewLineBefore;

        if (guide.positionIsEnd()) {
            // tabs can already be empty if the writeLine ended and left an ending
            // guide as the very last one

            return new NewLineDouble(guide.endWithNewLine(), writeNewLineBefore, false, true);
        }
        // MID
        if (!tabs.isEmpty()) {
            tabs.peek().seenFirstMid = true;
            tabs.peek().countLines = 0; // reset
        }
        return new NewLineDouble(true, writeNewLineBefore, true, false);
    }

    /**
     * Makes no decisions, simply writes all non-guides from start to end inclusive.
     *
     * @param list   the source
     * @param writer the output writer
     * @param start  the position in the source to start
     * @param end    the position in the source to end
     * @throws IOException when something goes wrong writing to <code>writer</code>
     */
    void writeLine(List<OutputElement> list, Writer writer, int start, int end) throws IOException {
        try {
            Forward.forward(runtime, options, list, forwardInfo -> {
                try {
                    if (forwardInfo.guide() == null) {
                        writer.write(forwardInfo.string());
                    }
                    return forwardInfo.pos() == end;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, start, Math.max(100, options.lengthOfLine() * 3) / 2);
        } catch (RuntimeException e) {
            throw new IOException(e);
        }
    }

    static void indent(int spaces, Writer writer) throws IOException {
        for (int i = 0; i < spaces; i++) writer.write(" ");
    }
}
