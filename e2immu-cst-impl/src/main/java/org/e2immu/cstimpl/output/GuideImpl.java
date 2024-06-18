/*
 * e2immu: a static code analyser for effective and eventual immutability
 * Copyright 2020-2021, Bart Naudts, https://www.e2immu.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details. You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.e2immu.cstimpl.output;


import org.e2immu.annotation.ImmutableContainer;
import org.e2immu.annotation.rare.IgnoreModifications;
import org.e2immu.cstapi.output.FormattingOptions;
import org.e2immu.cstapi.output.element.Guide;

import java.util.concurrent.atomic.AtomicInteger;

@ImmutableContainer
public record GuideImpl(String name,
                        int index,
                        Position position,
                        int tabs,
                        boolean prioritySplit,
                        boolean startWithNewLine,
                        boolean endWithNewLine,
                        boolean allowNewLineBefore) implements Guide {

    public GuideImpl {
        assert position != null;
    }

    @IgnoreModifications
    private static final AtomicInteger generator = new AtomicInteger();

    public enum Position {
        START("S"), MID(""), END("E");

        private final String msg;

        Position(String msg) {
            this.msg = msg;
        }

        public String msg() {
            return msg;
        }

    }

    public static GuideGenerator generatorForBlock() {
        return new GuideGenerator("block", 1, true, true, true, true);
    }

    public static GuideGenerator generatorForEnumDefinitions() {
        return new GuideGenerator("enum", 0, false, false, true, true);
    }

    public static GuideGenerator generatorForCompanionList() {
        return new GuideGenerator("companion", 0, true, false, false, true);
    }

    public static GuideGenerator defaultGuideGenerator() {
        return new GuideGenerator("default", 1, false, false, false, false);
    }

    public static GuideGenerator generatorForMultilineComment() {
        // EXPLAIN prioritySplit must be false, otherwise empty stack exception in Formatter; see TestFormatter5
        return new GuideGenerator("multiLineComment", 0, false, true, true, true);
    }

    public static GuideGenerator generatorForParameterDeclaration() {
        return new GuideGenerator("params", 1, false, true, false, false);
    }

    public static GuideGenerator generatorForAnnotationList() {
        return new GuideGenerator("annotations", 0, false, false, false, false);
    }

    @ImmutableContainer
    public static class GuideGenerator {
        public final String name;
        public final int index;
        private final int tabs;
        private final boolean prioritySplit;
        private final boolean startWithNewLine;
        private final boolean endWithNewLine;
        private final boolean allowNewLineBefore;

        private GuideGenerator(String name,
                               int tabs,
                               boolean prioritySplit,
                               boolean startWithNewLine,
                               boolean endWithNewLine,
                               boolean allowNewLineBefore) {
            index = generator.incrementAndGet();
            this.name = name;
            this.tabs = tabs;
            this.startWithNewLine = startWithNewLine;
            this.endWithNewLine = endWithNewLine;
            this.prioritySplit = prioritySplit;
            this.allowNewLineBefore = allowNewLineBefore;
        }

        public GuideImpl start() {
            return new GuideImpl(name, index, Position.START, tabs, prioritySplit, startWithNewLine, endWithNewLine, allowNewLineBefore);
        }

        public GuideImpl mid() {
            return new GuideImpl(name, index, Position.MID, tabs, prioritySplit, startWithNewLine, endWithNewLine, allowNewLineBefore);
        }

        public GuideImpl end() {
            return new GuideImpl(name, index, Position.END, tabs, prioritySplit, startWithNewLine, endWithNewLine, allowNewLineBefore);
        }

        public boolean keepGuidesWithoutMid() {
            return prioritySplit || startWithNewLine || endWithNewLine;
        }
    }


    @Override
    public String minimal() {
        return "";
    }

    @Override
    public int length(FormattingOptions options) {
        return 0;
    }

    @Override
    public String write(FormattingOptions options) {
        return "";
    }

    @Override
    public String generateJavaForDebugging() {
        return ".add(gg" + index + "." + (switch (position) {
            case START -> "start";
            case MID -> "mid";
            case END -> "end";
        }) + "()) // priority=" + prioritySplit + ", startNL=" + startWithNewLine + ", endNL=" + endWithNewLine;
    }
}
