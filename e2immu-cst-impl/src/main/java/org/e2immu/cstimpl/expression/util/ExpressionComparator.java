/*
 * e2immu: a static code analyser for effective and eventual immutability
 * Copyright 2020-2021, Bart Naudts, https://www.e2immu.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details. You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.e2immu.cstimpl.expression.util;

import org.e2immu.annotation.rare.IgnoreModifications;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.ExpressionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.IdentityHashMap;

public class ExpressionComparator implements Comparator<Expression> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionComparator.class);

    public static final int ORDER_CONSTANT_NULL = 30;
    public static final int ORDER_CONSTANT_BOOLEAN = 31;
    public static final int ORDER_CONSTANT_BYTE = 32;
    public static final int ORDER_CONSTANT_CHAR = 33;
    public static final int ORDER_CONSTANT_SHORT = 34;
    public static final int ORDER_CONSTANT_INT = 35;
    public static final int ORDER_CONSTANT_FLOAT = 36;
    public static final int ORDER_CONSTANT_LONG = 37;
    public static final int ORDER_CONSTANT_DOUBLE = 38;
    public static final int ORDER_CONSTANT_CLASS = 39;
    public static final int ORDER_CONSTANT_STRING = 40;
    public static final int ORDER_PRODUCT = 41;
    public static final int ORDER_DIVIDE = 42;
    public static final int ORDER_REMAINDER = 43;
    public static final int ORDER_SUM = 44;
    public static final int ORDER_BITWISE_AND = 45;
    public static final int ORDER_BITWISE_OR = 46;
    public static final int ORDER_BITWISE_XOR = 47;
    public static final int ORDER_SHIFT_LEFT = 48;
    public static final int ORDER_SHIFT_RIGHT = 49;
    public static final int ORDER_UNSIGNED_SHIFT_RIGHT = 50;

    public static final int ORDER_BOOLEAN_XOR = 51;

    // variables, types
    public static final int ORDER_ARRAY = 60;
    public static final int ORDER_INSTANCE = 61;
    public static final int ORDER_NEW_INSTANCE = 62;
    public static final int ORDER_ARRAY_LENGTH = 63;
    public static final int ORDER_INLINE_METHOD = 64;
    public static final int ORDER_METHOD = 65;
    public static final int ORDER_METHOD_REFERENCE = 66;
    public static final int ORDER_LAMBDA = 67;
    public static final int ORDER_VARIABLE = 68;
    public static final int ORDER_EXPANDED_VARIABLE = 69;
    public static final int ORDER_TYPE = 70;
    public static final int ORDER_DELAYED_EXPRESSION = 71;
    public static final int ORDER_DELAYED_WRAPPED_EXPRESSION = 72;
    public static final int ORDER_EMPTY_EXPRESSION = 73;
    public static final int ORDER_CONTRACT_MARK = 74;
    public static final int ORDER_MULTI_EXPRESSION = 75;
    public static final int ORDER_COMMA = 76;
    public static final int ORDER_LOCAL_VAR_CREATION = 77;
    public static final int ORDER_UNKNOWN = 78;
    public static final int ORDER_SWITCH = 79;

    // boolean operations
    public static final int ORDER_INSTANCE_OF = 81;
    public static final int ORDER_EQUALS = 82;
    public static final int ORDER_GEQ0 = 82; // equality and inequality at the same level
    public static final int ORDER_OR = 85;
    public static final int ORDER_AND = 86;

    // must be later than any other binary operator (unevaluated)
    public static final int ORDER_BINARY_OPERATOR = 87;
    public static final int ORDER_UNARY_OPERATOR = 88;


    // irrelevant, normally
    public static final int ORDER_MVP = 90;
    public static final int ORDER_ANNOTATION_EXPRESSION = 91;

    public static final ExpressionComparator SINGLETON = new ExpressionComparator();

    private ExpressionComparator() {
        // nothing here
    }

    private record Unwrapped(Expression value, int count) {

        public static Unwrapped create(Expression v) {
            Expression unwrapped = v;
            int count = 0;
            while (unwrapped instanceof ExpressionWrapper e) {
                unwrapped = e.expression();
                count++;
            }
            return new Unwrapped(unwrapped, count);
        }
    }

    @IgnoreModifications
    private final IdentityHashMap<Expression, IdentityHashMap<Expression, Integer>> cache = new IdentityHashMap<>();

    @Override
    public int compare(Expression v1, Expression v2) {
        if (cache.size() > 1_000) cache.clear();
        IdentityHashMap<Expression, Integer> map = cache.computeIfAbsent(v1, e -> new IdentityHashMap<>());
        return map.computeIfAbsent(v2, e -> compareNotCached(v1, v2));
    }

    private int compareNotCached(Expression v1, Expression v2) {
        boolean v1Wrapped = v1 instanceof ExpressionWrapper;
        boolean v2Wrapped = v2 instanceof ExpressionWrapper;

        // short-cut
        if (!v1Wrapped && !v2Wrapped) {
            return compareWithoutWrappers(v1, v2);
        }

        Unwrapped u1 = Unwrapped.create(v1);
        Unwrapped u2 = Unwrapped.create(v2);

        int withoutWrappers = compareWithoutWrappers(u1.value, u2.value);
        if (withoutWrappers != 0) return withoutWrappers;

        if (!v1Wrapped) return -1; // unwrapped always before wrapped
        if (!v2Wrapped) return 1;

        // now both are wrapped...
        int w = ((ExpressionWrapper) v1).wrapperOrder() - ((ExpressionWrapper) v2).wrapperOrder();

        // different type of wrappers
        if (w != 0) return w;

        // same type of wrapper, but maybe different specifics (PropertyWrapper has a lot of fields, Negation has none)
        try {
            int u = v1.internalCompareTo(v2);
            if (u != 0) return u;
        } catch (InternalCompareToException ie) {
            LOGGER.error("Comparison at order {}", v1.order());
            LOGGER.error("Expression 1: '{}' of {}", v1, v1.getClass());
            LOGGER.error("Expression 2: '{}' of {}", v2, v2.getClass());
            throw new UnsupportedOperationException();
        }

        if (u1.count == 1 && u2.count == 1) {
            // everything has been compared, no need to delve deeper
            return 0;
        }
        // same wrappers, go deeper (v1 may have one wrapper, v2 may have 2)
        return compare(((ExpressionWrapper) v1).expression(), ((ExpressionWrapper) v2).expression());
    }

    private int compareWithoutWrappers(Expression v1, Expression v2) {
        int orderDiff = v1.order() - v2.order();
        if (orderDiff != 0) return orderDiff;
        try {
            Expression c1 = v1.conditionOfInlineConditional();
            Expression c2 = v2.conditionOfInlineConditional();
            if (c1 != null && c2 == null) {
                return c1.internalCompareTo(v2);
            }
            if (c2 != null && c1 == null) {
                return v1.internalCompareTo(c2);
            }
            return v1.internalCompareTo(v2);
        } catch (InternalCompareToException ie) {
            LOGGER.error("""
                    Comparison at order {}
                    Expression 1: '{}' of {}
                    Expression 2: '{}' of {}
                    v1.equals(v2): {}
                    """, v1.order(), v1, v1.getClass(), v2, v2.getClass(), v1.equals(v2));
            throw new UnsupportedOperationException();
        }
    }
}
