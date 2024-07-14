package org.e2immu.language.cst.api.type;

import org.e2immu.language.cst.api.output.element.Keyword;

public interface TypeNature {
    boolean isClass();

    boolean isEnum();

    boolean isInterface();

    boolean isRecord();

    boolean isStatic(); // is true for all but inner classes in Java

    boolean isAnnotation();

    Keyword keyword();
}
