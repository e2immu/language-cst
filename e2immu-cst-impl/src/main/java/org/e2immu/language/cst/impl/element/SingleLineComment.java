package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SpaceEnum;
import org.e2immu.language.cst.impl.output.SymbolEnum;
import org.e2immu.language.cst.impl.output.TextImpl;

public class SingleLineComment implements Comment {
    private final String comment;
    private final Source source;

    public SingleLineComment(Source source, String comment) {
        this.comment = strip(comment.trim());
        this.source = source;
    }

    private static String strip(String s) {
        String leading = SymbolEnum.SINGLE_LINE_COMMENT.symbol();
        if (s.startsWith(leading)) return s.substring(leading.length());
        return s;
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder ob = new OutputBuilderImpl().add(SymbolEnum.SINGLE_LINE_COMMENT);
        if (!comment.isEmpty()) ob.add(new TextImpl(comment));
        return ob.add(SpaceEnum.NEWLINE);
    }

    @Override
    public String comment() {
        return comment;
    }

    @Override
    public Source source() {
        return source;
    }

    @Override
    public String toString() {
        return "singleLineComment@" + source.compact2();
    }
}
