package org.e2immu.cstimpl.element;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SpaceEnum;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.output.TextImpl;

public class SingleLineComment implements Comment {
    private final String comment;

    public SingleLineComment(String comment) {
        this.comment = strip(comment.trim());
    }

    private static String strip(String s) {
        String leading = SymbolEnum.SINGLE_LINE_COMMENT.symbol();
        if (s.startsWith(leading)) return s.substring(leading.length());
        return s;
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(SymbolEnum.SINGLE_LINE_COMMENT).add(new TextImpl(comment)).add(SpaceEnum.NEWLINE);
    }
}
