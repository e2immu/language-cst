package org.e2immu.language.cst.api.type;

public interface TypeNature {
    boolean isClass();

    boolean isEnum();

    boolean isInterface();

    boolean isStatic(); // is true for all but inner classes in Java

    boolean isAnnotation();
}
