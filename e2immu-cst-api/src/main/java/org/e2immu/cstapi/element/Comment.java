package org.e2immu.cstapi.element;

import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;

public interface Comment {
    OutputBuilder print(Qualification qualification);
}
