package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.output.element.Keyword;
import org.e2immu.language.cst.api.type.TypeNature;
import org.e2immu.language.cst.impl.output.KeywordImpl;

public enum TypeNatureEnum implements TypeNature {

    ANNOTATION(KeywordImpl.AT_INTERFACE),
    CLASS(KeywordImpl.CLASS),
    ENUM(KeywordImpl.ENUM),
    INTERFACE(KeywordImpl.INTERFACE),
    PRIMITIVE(null),
    RECORD(KeywordImpl.RECORD);

    private final Keyword keyword;

    TypeNatureEnum(Keyword keyword) {
        this.keyword = keyword;
    }

    public boolean isFinal() {
        return this != CLASS && this != INTERFACE;
    }

    @Override
    public boolean isClass() {
        return this == CLASS;
    }

    @Override
    public boolean isInterface() {
        return this == INTERFACE;
    }

    @Override
    public boolean isEnum() {
        return this == ENUM;
    }

    @Override
    public boolean isStatic() {
        return this != CLASS;
    }

    public Keyword keyword() {
        return keyword;
    }

    @Override
    public boolean isAnnotation() {
        return this == ANNOTATION;
    }
}
