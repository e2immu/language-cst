package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.impl.output.*;

import java.util.Arrays;

public class MultiLineComment implements Comment {
    private final String comment;
    private final Source source;

    public MultiLineComment(Source source, String comment) {
        this.comment = strip(comment.trim());
        this.source = source;
    }

    private static String strip(String s) {
        String leading = SymbolEnum.LEFT_BLOCK_COMMENT.symbol();
        String s1 = s.startsWith(leading) ? s.substring(leading.length()) : s;
        String trailing = SymbolEnum.RIGHT_BLOCK_COMMENT.symbol();
        return s1.endsWith(trailing) ? s1.substring(0, s1.length() - trailing.length()) : s1;
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        boolean multiLine = comment.contains("\n");
        GuideImpl.GuideGenerator gg = multiLine ? GuideImpl.generatorForMultilineComment()
                : GuideImpl.defaultGuideGenerator();
        OutputBuilder joinedText = Arrays.stream(comment.split("\n"))
                .filter(line -> !line.isBlank())
                .map(line -> new OutputBuilderImpl().add(new TextImpl(line)))
                .collect(OutputBuilderImpl.joining(SpaceEnum.ONE_IS_NICE_EASY_SPLIT,
                        SymbolEnum.LEFT_BLOCK_COMMENT,
                        SymbolEnum.RIGHT_BLOCK_COMMENT,
                        gg));
        return new OutputBuilderImpl().add(joinedText);
    }

    @Override
    public String comment() {
        return comment;
    }

    @Override
    public Source source() {
        return source;
    }
}
