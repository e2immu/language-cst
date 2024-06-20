package org.e2immu.language.cst.api.output.element;

public interface Split {
    Split easiest(Split split);

    int rank();
}
