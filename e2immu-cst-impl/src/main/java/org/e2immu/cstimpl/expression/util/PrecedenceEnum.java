package org.e2immu.cstimpl.expression.util;

import org.e2immu.cstapi.expression.Precedence;

public enum PrecedenceEnum implements Precedence {

    TOP(20), // constants
    ARRAY_ACCESS(19), // method invoke, object member access [] () .
    PLUSPLUS(18), // unary -, + and ++, --
    UNARY(17), // ! ~ (cast) new
    MULTIPLICATIVE(16), // * % /
    ADDITIVE(15), // + -
    STRING_CONCAT(14), // +
    SHIFT(13), // << >> >>>
    RELATIONAL(12), // < <= > >=
    INSTANCE_OF(11), // instanceof
    EQUALITY(10), // == !=
    AND(9), // &
    XOR(8), // ^
    OR(7), // |
    LOGICAL_AND(6), // &&
    LOGICAL_OR(5), // ||
    TERNARY(4), // ?:
    ASSIGNMENT(3), // =
    COMPOUND_ASSIGNMENT_1(2), // += -= *= %= /= &=
    COMPOUND_ASSIGNMENT_2(1), // ^= |= <<= >>= >>>=
    BOTTOM(0) //
    ;

    private final int value;


    PrecedenceEnum(int value) {
        this.value = value;
    }

    @Override
    public int value() {
        return value;
    }
}
