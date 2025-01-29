package org.e2immu.language.cst.api.element;

import java.util.List;

public interface DetailedSources {
    Source detail(Object object);

    // use for types
    List<Source> details(Object object);

    interface Builder {

        Builder copy();

        Builder put(Object object, Source source);

        DetailedSources build();
    }
}
