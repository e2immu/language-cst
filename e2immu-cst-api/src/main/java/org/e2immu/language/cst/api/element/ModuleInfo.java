package org.e2immu.language.cst.api.element;

import java.util.List;

public interface ModuleInfo extends Element {
    List<Requires> requires();

    String name();

    interface Builder extends Element.Builder<Builder> {
        void addRequires(String name, boolean isStatic, boolean isTransitive);

        Builder setName(String name);

        ModuleInfo build();
    }

    interface Requires {
        boolean isTransitive();

        boolean isStatic();

        String name();
    }
}
