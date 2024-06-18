package org.e2immu.language.cst.api.info;

public interface Access {

    int level();

    Access combine(Access other);

    default boolean le(Access other) {
        return level() <= other.level();
    }

    boolean isPublic();

    boolean isPrivate();

    boolean isProtected();

    boolean isPackage();
}
