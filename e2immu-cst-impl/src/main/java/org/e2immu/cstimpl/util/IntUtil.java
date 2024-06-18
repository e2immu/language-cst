package org.e2immu.cstimpl.util;

public class IntUtil {

    private IntUtil() {
    }

    // copied from Guava, DoubleMath class
    public static boolean isMathematicalInteger(double x) {
        return !Double.isNaN(x) && !Double.isInfinite(x) && x == Math.rint(x);
    }
}
