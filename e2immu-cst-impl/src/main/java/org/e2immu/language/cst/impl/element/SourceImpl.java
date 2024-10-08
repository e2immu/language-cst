package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.CompilationUnit;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;

import java.util.Objects;

/*
we must be a bit memory-conscious: no unnecessary fields because there may be millions of elements
 */
public class SourceImpl implements Source {
    private final Element parent;
    private final String index;
    private final short beginLine;
    private final short beginPos;
    private final short endLine;
    private final short endPos;

    public SourceImpl(Element parent, String index, int beginLine, int beginPos, int endLine, int endPos) {
        this.parent = parent;
        // we internalize, because there are many repeats here ("0", "1", ...)
        this.index = index == null ? null : index.intern();
        this.beginLine = (short) beginLine;
        this.beginPos = (short) beginPos;
        this.endLine = (short) endLine;
        this.endPos = (short) endPos;
    }

    public static Source forCompiledClass(CompilationUnit compilationUnit) {
        return new SourceImpl(compilationUnit, null, -1, -1, -1, -1);
    }

    @Override
    public boolean isCompiledClass() {
        return beginLine == -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceImpl source)) return false;
        return beginLine == source.beginLine && beginPos == source.beginPos && endLine == source.endLine && endPos == source.endPos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginLine, beginPos, endLine, endPos);
    }

    @Override
    public int compareTo(Source o) {
        if (o instanceof SourceImpl s) {
            int bl = beginLine - s.beginLine;
            if (bl != 0) return bl;
            int bp = beginPos - s.beginPos;
            if (bp != 0) return bp;
            int el = endLine - s.endLine;
            if (el != 0) return el;
            return endPos - endLine;
        } else throw new UnsupportedOperationException();
    }

    @Override
    public Element parent() {
        return parent;
    }

    @Override
    public int beginLine() {
        return beginLine;
    }

    @Override
    public int beginPos() {
        return beginPos;
    }

    @Override
    public int endLine() {
        return endLine;
    }

    @Override
    public int endPos() {
        return endPos;
    }

    @Override
    public String index() {
        return index;
    }

    @Override
    public String toString() {
        return index + "@" + beginLine + ":" + beginPos + "-" + endLine + ":" + endPos;
    }

    @Override
    public Source withIndex(String newIndex) {
        return new SourceImpl(parent, newIndex, beginLine, beginPos, endLine, endPos);
    }
}
