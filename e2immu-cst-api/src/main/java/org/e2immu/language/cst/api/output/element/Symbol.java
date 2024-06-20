package org.e2immu.language.cst.api.output.element;

import org.e2immu.language.cst.api.output.OutputElement;

public interface Symbol extends OutputElement {

    String symbol();

    Space left();

    Space right();

    String constant();
}
