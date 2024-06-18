package org.e2immu.cstimpl.expression.eval;

import org.e2immu.cstapi.expression.*;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstimpl.expression.ExpressionImpl;
import org.e2immu.cstimpl.expression.OrImpl;
import org.e2immu.cstimpl.expression.util.AndOrSorter;
import org.e2immu.cstimpl.util.IntUtil;
import org.e2immu.cstimpl.util.ListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.e2immu.cstimpl.expression.ExpressionCanBeTooComplex.reducedComplexity;

public class EvalOr {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvalOr.class);

    private final Runtime runtime;

    public EvalOr(Runtime runtime) {
        this.runtime = runtime;
    }

    public Expression eval(List<Expression> values) {

        // STEP 1: trivial reductions

        if (values.size() == 1) {
            if (values.get(0).isInstanceOf(Or.class) || values.get(0).isInstanceOf(And.class)) {
                LOGGER.debug("Return immediately in Or: {}", values.get(0));
                return values.get(0);
            }
        }

        // STEP 2: concat everything

        ArrayList<Expression> concat = new ArrayList<>(values.size());
        recursivelyAdd(concat, values);

        // STEP 3: loop

        And firstAnd = null;

        int complexity = values.stream().mapToInt(Expression::complexity).sum();
        boolean changes = complexity < runtime.limitOnComplexity();
        if (!changes) {
            LOGGER.debug("Not analysing OR operation, complexity {}", complexity);
            return reducedComplexity(runtime, values);
        }
        assert complexity < ExpressionImpl.HARD_LIMIT_ON_COMPLEXITY : "Complexity reached " + complexity;

        while (changes) {
            changes = false;

            // STEP 4a: sort

            concat = AndOrSorter.sort(concat);

            // STEP 4b: observations

            for (Expression value : concat) {
                if (value instanceof BooleanConstant bc && bc.constant()) {
                    LOGGER.debug("Return TRUE in Or, found TRUE");
                    return runtime.constantTrue();
                }
            }
            concat.removeIf(value -> value instanceof BooleanConstant); // FALSE can go

            // STEP 4c: reductions

            ArrayList<Expression> newConcat = new ArrayList<>(concat.size());
            Expression prev = null;
            for (Expression value : concat) {

                // this works because of sorting
                // A || !A will always sit next to each other
                if (value instanceof Negation ne && ne.expression().equals(prev)) {
                    LOGGER.debug("Return TRUE in Or, found opposites {}", value);
                    return runtime.constantTrue();
                }

                GreaterThanZero gt0;
                GreaterThanZero gt1;
                if ((gt1 = value.asInstanceOf(GreaterThanZero.class)) != null
                    && prev != null
                    && (gt0 = prev.asInstanceOf(GreaterThanZero.class)) != null) {
                    GreaterThanZero.XB xb0 = gt0.extract(runtime);
                    GreaterThanZero.XB xb1 = gt1.extract(runtime);
                    if (xb0.x().equals(xb1.x())) {

                        // x>=a || x <= a-1
                        if (xb0.lessThan() == !xb1.lessThan() && orComparisonTrue(xb0.lessThan(), xb0.b(), xb1.b())) {
                            return runtime.constantTrue();
                        }
                        // x<=a || x<=b --> x<=max(a,b)
                        if (xb0.lessThan() && xb1.lessThan()) {
                            changes = true;
                            if (xb0.b() < xb1.b()) {
                                // replace previous
                                newConcat.set(newConcat.size() - 1, value);
                            }  // else ignore this one
                            continue;
                        }

                        // x>=a || x>=b --> x>=min(a,b)
                        if (!xb0.lessThan() && !xb1.lessThan()) {
                            changes = true;
                            if (xb0.b() > xb1.b()) {
                                // replace previous
                                newConcat.set(newConcat.size() - 1, value);
                            }  // else ignore this one
                            continue;
                        }
                    }
                }

                // A || A
                And andValue;
                if (value.equals(prev)) {
                    changes = true;
                } else if ((andValue = value.asInstanceOf(And.class)) != null) {
                    if (andValue.expressions().size() == 1) {
                        newConcat.add(andValue.expressions().get(0));
                        changes = true;
                    } else if (firstAnd == null) {
                        firstAnd = andValue;
                        changes = true;
                    } else {
                        newConcat.add(andValue); // for later
                    }
                } else {
                    newConcat.add(value);
                }
                prev = value;
            }
            concat = newConcat;
        }
        ArrayList<Expression> finalValues = concat;
        if (firstAnd != null) {
            List<Expression> components = firstAnd.expressions().stream()
                    .map(v -> runtime.and(ListUtil.immutableConcat(finalValues, List.of(v))))
                    .toList();
            LOGGER.debug("Found And-clause {}, components for new And are {}", firstAnd, components);
            int complexityComponents = components.stream().mapToInt(Expression::complexity).sum();
            if (complexityComponents < runtime.limitOnComplexity()) {
                return runtime.and(components);
            }
        }
        if (finalValues.size() == 1) return finalValues.get(0);

        for (Expression value : finalValues) {
            if (value.isEmpty()) throw new UnsupportedOperationException();
        }

        if (finalValues.isEmpty()) {
            LOGGER.debug("Empty disjunction returned as false");
            return runtime.constantFalse();
        }
        return new OrImpl(runtime, finalValues);
    }

    protected boolean orComparisonTrue(boolean d0IsLt, double d0, double d1) {
        boolean i0 = IntUtil.isMathematicalInteger(d0);
        boolean i1 = IntUtil.isMathematicalInteger(d1);
        if (i0 && i1) {
            if (d0IsLt) {
                //d0IsLt == true: x <= 4 || x >= 5
                return d1 - 1 <= d0; // 5-1<=4, 3-1<=4 but not 10-1<=4
            }
            // d0IsLt == false: x >= 4 || x <= 3
            return d0 - 1 <= d1; // 4-1 <= 3  1-1<=3 but not 10-1<= 3
        }
        return d0 == d1;
    }

    protected void recursivelyAdd(ArrayList<Expression> concat, List<Expression> collect) {
        for (Expression value : collect) {
            Or or;
            if ((or = value.asInstanceOf(Or.class)) != null) {
                concat.addAll(or.expressions());
            } else {
                concat.add(value);
            }
        }
    }

}
