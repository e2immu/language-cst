package org.e2immu.language.cst.api.element;

import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;

public interface Comment {
    OutputBuilder print(Qualification qualification);

    String comment();
}
