package org.e2immu.cstapi.expression;

import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;

public interface Expression extends Comparable<Expression>, Element {

    ParameterizedType parameterizedType();

    Precedence precedence();

    // "external": helps to compare expressions of different types
    int order();

    // "internal" as: how do two expressions of the same type compare to each other?
    int internalCompareTo(Expression expression);

    // convenience methods

    default boolean isBoolValueTrue() {
        return false;
    }

    default boolean isBoolValueFalse() {
        return false;
    }

    default boolean isBooleanConstant() {
        return false;
    }

    default boolean isNullConstant() {
        return false;
    }

    default boolean isEmpty() {
        return false;
    }

    default boolean isConstant() {
        return false;
    }

    default boolean isNegatedOrNumericNegative() {
        return false;
    }

    default Double numericValue() {
        return null;
    }
    default boolean isNumeric() {
        return false;
    }

    default Expression conditionOfInlineConditional() {
        return null;
    }

    Expression translate(TranslationMap translationMap);
}
