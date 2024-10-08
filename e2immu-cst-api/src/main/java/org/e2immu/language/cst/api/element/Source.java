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

    // computations: override if you need to use these frequently

    default Info info() {
        Element parent = parent();
        if (parent instanceof Info info) return info;
        if (parent == null) return null;
        Source parentSource = parent.source();
        return parentSource == null ? null : parentSource.info();
    }

    default CompilationUnit compilationUnit() {
        if(parent() instanceof CompilationUnit cu) return cu;
        Info info = info();
        return info == null ? null : info.compilationUnit();
    }

    default String compact() {
        return beginLine() + "-" + beginPos();
    }

    Source withIndex(String newIndex);
}
