package org.e2immu.cstapi.type;

public interface Wildcard {
    boolean isUnbound();

    boolean isExtends();

    boolean isSuper();
}
