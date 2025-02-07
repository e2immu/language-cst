package org.e2immu.language.cst.api.element;

import java.util.List;

public interface DetailedSources {
    // used to grab the closing parenthesis of the record field list,
    // the closing parenthesis of any method declaration's form parameter list
    Object END_OF_PARAMETER_LIST = new Object();

    Source detail(Object object);

    // use for types
    List<Source> details(Object object);

    interface Builder {

        Builder copy();

        Builder put(Object object, Source source);

        DetailedSources build();
    }
}
