package org.e2immu.cstapi.info;

public interface MethodModifier {

    boolean isPublic();

    boolean isPrivate();

    boolean isProtected();

    boolean isAbstract();

    boolean isDefault();

    boolean isSynchronized();

    boolean isFinal();

    boolean isStatic();
}
