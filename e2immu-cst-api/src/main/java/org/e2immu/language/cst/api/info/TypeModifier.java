package org.e2immu.language.cst.api.info;

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
