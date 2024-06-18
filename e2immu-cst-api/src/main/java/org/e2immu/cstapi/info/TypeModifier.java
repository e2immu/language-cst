package org.e2immu.cstapi.info;

public interface TypeModifier {
    boolean isPublic();

    boolean isPrivate();

    boolean isProtected();

    boolean isAbstract();

    boolean isFinal();

    boolean isStatic();

    boolean isSealed();

    boolean isNonSealed();
}
