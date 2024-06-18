package org.e2immu.cstimpl.type;

import org.e2immu.cstapi.type.Wildcard;

public enum WildcardEnum implements Wildcard {
    EXTENDS, SUPER, UNBOUND,
    ;

    @Override
    public boolean isUnbound() {
        return this == UNBOUND;
    }

    @Override
    public boolean isExtends() {
        return this == EXTENDS;
    }

    @Override
    public boolean isSuper() {
        return this == SUPER;
    }
}
