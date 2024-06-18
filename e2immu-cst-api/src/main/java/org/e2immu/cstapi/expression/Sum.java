package org.e2immu.cstapi.expression;


import org.e2immu.cstapi.runtime.Runtime;

public interface Sum extends BinaryOperator {

    Double numericPartOfLhs();
    Expression nonNumericPartOfLhs(Runtime runtime);
    Expression isZero(Runtime runtime);
}
