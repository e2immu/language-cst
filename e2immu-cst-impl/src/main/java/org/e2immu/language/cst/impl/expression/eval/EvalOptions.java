package org.e2immu.language.cst.impl.expression.eval;

public record EvalOptions(int maxComplexityOrExpansion,
                          double maxFactorOrExpansion,
                          int maxCombinationsOrExpansion,
                          int maxAndOrComplexity) {
    public static EvalOptions DEFAULT = new EvalOptions(150,
            2.5d, 50, 150);
}
