package org.e2immu.language.cst.api.element;

import org.e2immu.language.cst.api.info.Info;

public interface Source extends Comparable<Source> {

    // for all elements
    Element parent();

    int beginLine();

    int beginPos();

    int endLine();

    int endPos();

    // for statements only
    String index();

    boolean isCompiledClass();

    DetailedSources detailedSources();

    Source withDetailedSources(DetailedSources detailedSources);

    Source mergeDetailedSources(DetailedSources detailedSources);

    // computations: override if you need to use these frequently

    default Info info() {
        Element parent = parent();
        if (parent instanceof Info info) return info;
        if (parent == null) return null;
        Source parentSource = parent.source();
        return parentSource == null ? null : parentSource.info();
    }

    default CompilationUnit compilationUnit() {
        if (parent() instanceof CompilationUnit cu) return cu;
        Info info = info();
        return info == null ? null : info.compilationUnit();
    }

    default String compact() {
        return beginLine() + "-" + beginPos();
    }

    default String compact2() {
        return beginLine() + "-" + beginPos() + ":" + endLine() + "-" + endPos();
    }

    Source withIndex(String newIndex);

    default boolean isContainedIn(int line, int pos) {
        if (line > beginLine() && line < endLine()) return true;
        if (line < beginLine() || line > endLine()) return false;
        if (line == beginLine() && line == endLine()) return beginPos() <= pos && pos <= endPos();
        if (line == beginLine()) return beginPos() <= pos;
        return pos <= endPos();
    }

}
