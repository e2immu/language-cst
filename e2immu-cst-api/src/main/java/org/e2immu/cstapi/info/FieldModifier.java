package org.e2immu.cstapi.info;

public interface FieldModifier {
    boolean isStatic();

    boolean isFinal();

    boolean isVolatile();

    boolean isTransient();

    boolean isPublic();

    boolean isPrivate();

    boolean isProtected();
}
