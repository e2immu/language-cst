package org.e2immu.cstimpl.expression.eval;

import org.e2immu.cstapi.expression.*;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.expression.AndImpl;
import org.e2immu.cstimpl.expression.ExpressionImpl;
import org.e2immu.cstimpl.expression.SumImpl;
import org.e2immu.cstimpl.expression.util.AndOrSorter;
import org.e2immu.cstimpl.expression.util.InequalitySolver;
import org.e2immu.cstimpl.expression.util.LhsRhs;
import org.e2immu.cstimpl.util.IntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.e2immu.cstimpl.expression.ExpressionCanBeTooComplex.reducedComplexity;

public class EvalAnd {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvalAnd.class);

    private final Runtime runtime;

    public EvalAnd(Runtime runtime) {
        this.runtime = runtime;
    }

    private enum Action {
        SKIP, REPLACE, FALSE, TRUE, ADD, ADD_CHANGE
    }

    // we try to maintain a CNF
    public Expression eval(List<Expression> values) {

        // STEP 1: check that all values return boolean!
        int complexity = 0;
        for (Expression v : values) {
            assert !v.isEmpty() : "Unknown value " + v + " in And";
            assert v.parameterizedType() != null : "Null return type for " + v + " in And";
            assert v.parameterizedType().isBooleanOrBoxedBoolean() || v.parameterizedType().isUnboundTypeParameter()
                    : "Non-boolean return type for " + v + " in And: " + v.parameterizedType();

            complexity += v.complexity();
        }

        // STEP 2: trivial reductions

        if (values.size() == 1 && values.get(0).isInstanceOf(And.class))
            return values.get(0);

        // STEP 3: concat everything

        ArrayList<Expression> concat = new ArrayList<>(values.size());
        recursivelyAdd(concat, values);

        // STEP 4: loop

        boolean changes = complexity < runtime.limitOnComplexity();
        if (!changes) {
            LOGGER.debug("Not analysing AND operation, complexity {}", complexity);
            return reducedComplexity(runtime, values);
        }
        assert complexity < ExpressionImpl.HARD_LIMIT_ON_COMPLEXITY : "Complexity reached " + complexity;

        while (changes) {
            changes = false;

            // STEP 4a: sort

            concat = AndOrSorter.sort(concat);

            // STEP 4b: observations

            for (Expression value : concat) {
                if (value instanceof BooleanConstant bc && !bc.constant()) {
                    LOGGER.debug("Return FALSE in And, found FALSE");
                    return runtime.constantFalse();
                }
            }
            concat.removeIf(value -> value instanceof BooleanConstant); // TRUE can go

            // STEP 4c: reductions

            ArrayList<Expression> newConcat = new ArrayList<>(concat.size());
            Expression prev = null;
            int pos = 0;
            for (Expression value : concat) {

                Action action = analyse(pos, newConcat, prev, value);
                switch (action) {
                    case FALSE:
                        return runtime.constantFalse();
                    case TRUE:
                        return runtime.constantTrue();
                    case ADD:
                        newConcat.add(value);
                        break;
                    case ADD_CHANGE:
                        newConcat.add(value);
                        changes = true;
                        break;
                    case REPLACE:
                        newConcat.set(newConcat.size() - 1, value);
                        changes = true;
                        break;
                    case SKIP:
                        changes = true;
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }

                prev = value;
                pos++;
            }
            concat = newConcat;
        }
        if (concat.isEmpty()) {
            LOGGER.debug("And reduced to 0 components, return true");
            return runtime.constantTrue();
        }
        if (concat.size() == 1) {
            LOGGER.debug("And reduced to 1 component: {}", concat.get(0));
            return concat.get(0);
        }
        And res = new AndImpl(runtime, List.copyOf(concat));
        LOGGER.debug("Constructed {}", res);
        return res;
    }

    private Action analyse(int pos, ArrayList<Expression> newConcat, Expression prev, Expression value) {
        // A && A
        if (value.equals(prev)) return Action.SKIP;

        // this works because of sorting
        // A && !A will always sit next to each other
        Negation negatedValue;
        if ((negatedValue = value.asInstanceOf(Negation.class)) != null && negatedValue.expression().equals(prev)) {
            LOGGER.debug("Return FALSE in And, found direct opposite for {}", value);
            return Action.FALSE;
        }

        // A && A ? B : C --> A && B
        InlineConditional conditionalValue;
        if ((conditionalValue = value.asInstanceOf(InlineConditional.class)) != null
            && conditionalValue.condition().equals(prev)) {
            newConcat.add(conditionalValue.ifTrue());
            return Action.SKIP;
        }
        // A ? B : C && !A --> !A && C
        InlineConditional conditionalValue2;
        if (prev != null
            && (conditionalValue2 = prev.asInstanceOf(InlineConditional.class)) != null
            && conditionalValue2.condition().equals(runtime.negate(value))) {
            newConcat.set(newConcat.size() - 1, conditionalValue2.ifFalse());
            return Action.ADD;
        }

        // A && (!A || ...) ==> we can remove the !A
        // if we keep doing this, the OrValue empties out, and we are in the situation:
        // A && !B && (!A || B) ==> each of the components of an OR occur in negative form earlier on
        // this is the more complicated form of A && !A
        if (value.isInstanceOf(Or.class)) {
            List<Expression> remaining = new ArrayList<>(components(value));
            Iterator<Expression> iterator = remaining.iterator();
            boolean changed = false;
            while (iterator.hasNext()) {
                Expression value1 = iterator.next();
                Expression negated1 = runtime.negate(value1);
                boolean found = false;
                for (int pos2 = 0; pos2 < newConcat.size(); pos2++) {
                    if (pos2 != pos && negated1.equals(newConcat.get(pos2))) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    iterator.remove();
                    changed = true;
                }
            }
            if (changed) {
                if (remaining.isEmpty()) {
                    LOGGER.debug("Return FALSE in And, found opposite for {}", value);
                    return Action.FALSE;
                }
                // replace
                Expression orValue = runtime.or(remaining);
                LOGGER.debug("Replace {} by {}, found opposite in {}", value, orValue, newConcat);
                newConcat.add(orValue);
                return Action.SKIP;
            }
        }

        // the more complicated variant of A && A => A
        // A && (A || xxx) ==> A
        if (value.isInstanceOf(Or.class)) {
            List<Expression> components = components(value);
            for (Expression value1 : components) {
                for (Expression value2 : newConcat) {
                    if (value1.equals(value2)) {
                        LOGGER.debug("Skipping {} in OR, already in other clause of And", value);
                        return Action.SKIP;
                    }
                }
            }
        }
        // A || B &&  A || !B ==> A
        if (value.isInstanceOf(Or.class) && prev != null && prev.isInstanceOf(Or.class)) {
            List<Expression> components = components(value);
            List<Expression> prevComponents = components(prev);
            List<Expression> equal = new ArrayList<>();
            boolean ok = true;
            for (Expression value1 : components) {
                if (prevComponents.contains(value1)) {
                    equal.add(value1);
                } else if (!prevComponents.contains(runtime.negate(value1))) {
                    // not opposite, e.g. C
                    ok = false;
                    break;
                }
            }
            if (ok && !equal.isEmpty()) {
                Expression orValue = runtime.or(equal);
                newConcat.set(newConcat.size() - 1, orValue);
                LOGGER.debug("Skipping {} in OR, simplified to {}", value, orValue);
                return Action.SKIP;
            }
        }

        // combinations with equality and inequality (GE)

        GreaterThanZero gt0;
        if ((gt0 = value.asInstanceOf(GreaterThanZero.class)) != null
            && gt0.expression().variableStreamDescend().count() > 1) {
            // it may be interesting to run the inequality solver
            InequalitySolver inequalitySolver = new InequalitySolver(runtime, newConcat);
            Boolean resolve = inequalitySolver.evaluate(value);
            if (resolve == Boolean.FALSE) return Action.FALSE;
        }

        Equals ev1;
        if (prev instanceof Negation negatedPrev && (ev1 = negatedPrev.expression().asInstanceOf(Equals.class)) != null) {
            Equals ev2;
            if ((ev2 = value.asInstanceOf(Equals.class)) != null) {
                // not (3 == a) && (4 == a)  (the situation 3 == a && not (3 == a) has been solved as A && not A == False
                if (ev1.rhs().equals(ev2.rhs()) && !ev1.lhs().equals(ev2.lhs())) {
                    newConcat.remove(newConcat.size() - 1); // full replace
                    return Action.ADD;
                }
            }
        }

        // x.equals(y)
        Action actionEqualsEquals = analyseEqualsEquals(prev, value, newConcat);
        if (actionEqualsEquals != null) return actionEqualsEquals;

        // x == y
        Action actionEqEq = analyseEqEq(prev, value, newConcat);
        if (actionEqEq != null) return actionEqEq;

        Action actionGeNotEqual = analyseGeNotEq(newConcat, prev, value);
        if (actionGeNotEqual != null) return actionGeNotEqual;

        Action actionGeGe = analyseGeGe(newConcat, prev, value);
        if (actionGeGe != null) return actionGeGe;

        Action actionInstanceOf = analyseInstanceOf(prev, value);
        if (actionInstanceOf != null) return actionInstanceOf;


        // simplification of the OrValue

        Or orValue;
        if ((orValue = value.asInstanceOf(Or.class)) != null) {
            if (orValue.expressions().size() == 1) {
                newConcat.add(orValue.expressions().get(0));
                LOGGER.debug("Simplification of OR into single And clause: {}", value);
                return Action.SKIP;
            }
        }

        return Action.ADD;
    }

    private Action analyseEqualsEquals(Expression prev,
                                       Expression value,
                                       ArrayList<Expression> newConcat) {
        LhsRhs ev1 = LhsRhs.equalsMethodCall(prev);
        if (ev1 != null && ev1.lhs().isConstant()) {
            Action a = equalsRhs(ev1, value);
            if (a != null) return a;

            return equalsAndOr(prev, value, newConcat, ev1.rhs());
        }
        return null;
    }

    private Action equalsRhs(LhsRhs ev1, Expression value) {
        LhsRhs ev2 = LhsRhs.equalsMethodCall(value);
        if (ev2 != null && ev2.lhs().isConstant()) {
            // "a".equals(s) && "b".equals(s)
            if (ev1.rhs().equals(ev2.rhs()) && !ev1.lhs().equals(ev2.lhs())) {
                return Action.FALSE;
            }
        }

        // EQ and NOT EQ
        LhsRhs ev2b;
        if (value instanceof Negation ne && ((ev2b = LhsRhs.equalsMethodCall(ne.expression())) != null)) {
            // "a".equals(s) && !"b".equals(s)
            if (ev1.rhs().equals(ev2b.rhs()) && !ev1.lhs().equals(ev2b.lhs())) {
                return Action.SKIP;
            }
        }
        return null;
    }

    private Action analyseEqEq(Expression prev, Expression value, ArrayList<Expression> newConcat) {
        Equals ev1;
        if (prev != null && (ev1 = prev.asInstanceOf(Equals.class)) != null) {
            Action skip = equalsAndOr(prev, value, newConcat, ev1.rhs());
            if (skip != null) return skip;
            Equals ev2;
            if ((ev2 = value.asInstanceOf(Equals.class)) != null) {
                // 3 == a && 4 == a
                if (ev1.rhs().equals(ev2.rhs()) && !ev1.lhs().equals(ev2.lhs())) {
                    return Action.FALSE;
                }
                // x == a%r && y == a
                Remainder remainder;
                if ((remainder = ev1.rhs().asInstanceOf(Remainder.class)) != null
                    && ev2.rhs().equals(remainder.lhs())) {
                    // let's evaluate x == y%r; if true, we can skip; if false, we can bail out
                    Expression yModR = runtime.remainder(ev2.lhs(), remainder.rhs());
                    if (yModR.isNumeric() && ev1.lhs().isNumeric()) {
                        if (yModR.equals(ev1.lhs())) {
                            return Action.REPLACE;
                        }
                        return Action.FALSE;
                    }
                    // this is a very limited implementation!!
                }
            }

            // EQ and NOT EQ
            Negation ne;
            Equals ev3;
            if ((ne = value.asInstanceOf(Negation.class)) != null
                && (ev3 = ne.expression().asInstanceOf(Equals.class)) != null) {
                // 3 == a && not (4 == a)  (the situation 3 == a && not (3 == a) has been solved as A && not A == False
                if (ev1.rhs().equals(ev3.rhs()) && !ev1.lhs().equals(ev3.lhs())) {
                    return Action.SKIP;
                }
            }

            // GE and EQ (note: GE always comes after EQ)
            GreaterThanZero ge;
            if ((ge = value.asInstanceOf(GreaterThanZero.class)) != null) {
                GreaterThanZero.XB xb = ge.extract(runtime);
                Numeric ev1ln;
                if ((ev1ln = ev1.lhs().asInstanceOf(Numeric.class)) != null && ev1.rhs().equals(xb.x())) {
                    double y = ev1ln.doubleValue();
                    if (xb.lessThan()) {
                        // y==x and x <= b
                        if (ge.allowEquals() && y <= xb.b() || !ge.allowEquals() && y < xb.b()) {
                            return Action.SKIP;
                        }
                    } else {
                        // y==x and x >= b
                        if (ge.allowEquals() && y >= xb.b() || !ge.allowEquals() && y > xb.b()) {
                            return Action.SKIP;
                        }
                    }
                    return Action.FALSE;
                }
            }
        }
        return null;
    }

    private Action equalsAndOr(Expression prev,
                               Expression value,
                               ArrayList<Expression> newConcat,
                               Expression equalityRhs) {
        Or or;
        if ((or = value.asInstanceOf(Or.class)) != null) {
            // do a check first -- should we expand?
            if (safeToExpandOr(equalityRhs, or)) {
                List<Expression> result = new ArrayList<>(or.expressions().size());
                boolean foundTrue = false;
                for (Expression clause : or.expressions()) {
                    Expression and = eval(List.of(prev, clause));
                    if (and.isBoolValueTrue()) {
                        foundTrue = true;
                        break;
                    }
                    if (!and.isBoolValueFalse()) {
                        result.add(and);
                    }
                }
                if (foundTrue) {
                    return Action.SKIP;
                }
                if (result.isEmpty()) {
                    return Action.FALSE;
                }
                if (result.size() < or.expressions().size()) {
                    Expression newOr = runtime.or(result);
                    newConcat.set(newConcat.size() - 1, newOr); // full replace
                    return Action.ADD_CHANGE;
                }
            }
        }
        return null;
    }

    // starting off with "x == a", we're looking for comparisons to "a", and equality with "a"
    public static boolean safeToExpandOr(Expression rhs, Or or) {
        return or.expressions().stream().allMatch(clause -> extract(clause).equals(rhs));
    }

    public static Expression extract(Expression e) {
        Equals equals;
        if ((equals = e.asInstanceOf(Equals.class)) != null) return equals.rhs();
        GreaterThanZero gt0;
        if ((gt0 = e.asInstanceOf(GreaterThanZero.class)) != null) {
            return extract(gt0.expression());
        }
        Negation negation;
        if ((negation = e.asInstanceOf(Negation.class)) != null) return extract(negation.expression());
        SumImpl sum;
        if ((sum = e.asInstanceOf(SumImpl.class)) != null && sum.lhs().isConstant()) return extract(sum.rhs());
        LhsRhs lhsRhs = LhsRhs.equalsMethodCall(e);
        if (lhsRhs != null) return lhsRhs.rhs();
        return e;
    }


    private Action analyseGeNotEq(ArrayList<Expression> newConcat, Expression prev, Expression value) {
        //  GE and NOT EQ
        GreaterThanZero ge;
        Negation prevNeg;
        Equals equalsValue;
        if ((ge = value.asInstanceOf(GreaterThanZero.class)) != null
            && prev != null
            && (prevNeg = prev.asInstanceOf(Negation.class)) != null
            && (equalsValue = prevNeg.expression().asInstanceOf(Equals.class)) != null) {
            GreaterThanZero.XB xb = ge.extract(runtime);
            if (equalsValue.lhs() instanceof Numeric eqLn && equalsValue.rhs().equals(xb.x())) {
                double y = eqLn.doubleValue();

                // y != x && -b + x >= 0, in other words, x!=y && x >= b
                if (ge.allowEquals() && y < xb.b() || !ge.allowEquals() && y <= xb.b()) {
                    return Action.REPLACE;
                }
                // if b==y then the end result should be x>b
                if (y == xb.b() && ge.allowEquals()) {
                    newConcat.remove(newConcat.size() - 1);
                    GreaterThanZero gt;
                    if (ge.expression().parameterizedType().equals(runtime.intParameterizedType())) {
                        Expression oneLess = runtime.sum(ge.expression(), runtime.intMinusOne());
                        gt = runtime.newGreaterThanZero(oneLess, true);
                    } else {
                        gt = runtime.newGreaterThanZero(ge.expression(), false);
                    }
                    newConcat.add(gt);
                    return Action.SKIP;
                }
            }
        }
        return null;
    }

    private Action analyseGeGe(ArrayList<Expression> newConcat, Expression prev, Expression value) {
        // GE and GE
        GreaterThanZero ge1;
        GreaterThanZero ge2;
        if ((ge2 = value.asInstanceOf(GreaterThanZero.class)) != null
            && prev != null
            && (ge1 = prev.asInstanceOf(GreaterThanZero.class)) != null) {
            GreaterThanZero.XB xb1 = ge1.extract(runtime);
            GreaterThanZero.XB xb2 = ge2.extract(runtime);
            Expression notXb2x = runtime.negate(xb2.x());
            Boolean reverse = xb1.x().equals(xb2.x()) ? Boolean.FALSE : xb1.x().equals(notXb2x) ? Boolean.TRUE : null;
            if (reverse != null) {
                Expression xb1x = xb1.x();
                double xb1b = xb1.b();
                double xb2b = reverse ? -xb2.b() : xb2.b();
                boolean xb1lt = xb1.lessThan();
                boolean xb2lt = reverse != xb2.lessThan();

                // x>= b1 && x >= b2, with < or > on either
                if (xb1lt && xb2lt) {
                    // x <= b1 && x <= b2
                    // (1) b1 > b2 -> keep b2
                    if (xb1b > xb2b) return Action.REPLACE;
                    // (2) b1 < b2 -> keep b1
                    if (xb1b < xb2b) return Action.SKIP;
                    if (ge1.allowEquals()) return Action.REPLACE;
                    return Action.SKIP;
                }
                if (!xb1lt && !xb2lt) {
                    // x >= b1 && x >= b2
                    // (1) b1 > b2 -> keep b1
                    if (xb1b > xb2b) return Action.SKIP;
                    // (2) b1 < b2 -> keep b2
                    if (xb1b < xb2b) return Action.REPLACE;
                    // (3) b1 == b2 -> > or >=
                    if (ge1.allowEquals()) return Action.REPLACE;
                    return Action.SKIP;
                }

                // !xb1.lessThan: x >= b1 && x <= b2; otherwise: x <= b1 && x >= b2
                if (xb1b > xb2b) return !xb1lt ? Action.FALSE : Action.ADD;
                if (xb1b < xb2b) return !xb1lt ? Action.ADD : Action.FALSE;
                if (IntUtil.isMathematicalInteger(xb1b)) {
                    Expression newValue = runtime.equals(runtime.intOrDouble(xb1b), xb1x); // null-checks are irrelevant here
                    newConcat.set(newConcat.size() - 1, newValue);
                    return Action.SKIP;
                }
                return Action.FALSE;
            }
            Expression notGe2 = runtime.negate(ge2.expression());
            if (ge1.expression().equals(notGe2)) {
                if (ge1.allowEquals() && ge2.allowEquals()) {
                    // x >= 0, x <= 0 ==> x == 0
                    Expression result;
                    if (ge1.expression() instanceof SumImpl sum) {
                        result = sum.isZero(runtime);
                    } else {
                        result = runtime.equals(ge1.expression(), runtime.intZero());
                    }
                    newConcat.set(newConcat.size() - 1, result);
                    return Action.SKIP;
                }
                return Action.FALSE;
            }
        }
        return null;
    }

    private Action analyseInstanceOf(Expression prev, Expression value) {
        // a instanceof A && a instanceof B
        if (value instanceof InstanceOf i1 && prev instanceof InstanceOf i2 && i1.expression().equals(i2.expression())) {
            if (i1.parameterizedType().isAssignableFrom(runtime, i2.parameterizedType())) {
                // i1 is the most generic, so skip
                return Action.SKIP;
            }
            if (i2.parameterizedType().isAssignableFrom(runtime, i1.parameterizedType())) {
                // i2 is the most generic, so keep current
                return Action.REPLACE;
            }
            return Action.FALSE;
        }

        // a instanceof A && !(a instanceof B)
        // is written as: a instanceof A && (null==a||!(a instanceof B))
        InstanceOf negI1 = isNegationOfInstanceOf(value);
        if (negI1 != null && prev instanceof InstanceOf i2 && negI1.expression().equals(i2.expression())) {
            if (negI1.parameterizedType().isAssignableFrom(runtime, i2.parameterizedType())) {
                // B is the most generic, so we have a contradiction
                return Action.FALSE;
            }
            if (i2.parameterizedType().isAssignableFrom(runtime, negI1.parameterizedType())) {
                // i1 is the most generic, i2 is more specific; we keep what we have
                return Action.ADD;
            }
            // A unrelated to B, we drop the negation
            return Action.SKIP;
        }

        // !(a instanceof A) && a instanceof B
        InstanceOf negI2 = isNegationOfInstanceOf(prev);
        if (value instanceof InstanceOf i1 && negI2 != null && negI2.expression().equals(i1.expression())) {
            if (negI2.parameterizedType().isAssignableFrom(runtime, i1.parameterizedType())) {
                // B is the most generic, so we have a contradiction
                return Action.FALSE;
            }
            if (i1.parameterizedType().isAssignableFrom(runtime, negI2.parameterizedType())) {
                // i1 is the most generic, i2 is more specific; we keep what we have
                return Action.ADD;
            }
            // A unrelated to B, we drop the negation
            return Action.REPLACE;
        }

        // null != a && a instanceof B
        InstanceOf i;
        Variable v;
        Negation neg;
        Equals eq;
        Variable ve;
        if ((i = value.asInstanceOf(InstanceOf.class)) != null
            && (v = isVariableExpression(i.expression())) != null
            && prev != null
            && (neg = prev.asInstanceOf(Negation.class)) != null
            && (eq = neg.expression().asInstanceOf(Equals.class)) != null
            && eq.lhs().isNullConstant()
            && (ve = isVariableExpression(eq.rhs())) != null
            && ve.equals(v)) {
            // remove previous
            return Action.REPLACE;
        }
        // null == a && a instanceof B
        InstanceOf i2;
        Variable v2;
        Equals eq2;
        Variable ve2;
        if ((i2 = value.asInstanceOf(InstanceOf.class)) != null
            && (v2 = isVariableExpression(i2.expression())) != null
            && prev != null
            && (eq2 = prev.asInstanceOf(Equals.class)) != null
            && eq2.lhs().isNullConstant()
            && (ve2 = isVariableExpression(eq2.rhs())) != null
            && v2.equals(ve2)) {
            // remove previous
            return Action.FALSE;
        }
        return null;
    }

    // can be extended to include "IsVariableExpression"
    protected Variable isVariableExpression(Expression expression) {
        VariableExpression ve = expression.asInstanceOf(VariableExpression.class);
        return ve == null ? null : ve.variable();
    }

    // a instanceof A && !(a instanceof B)
    // is written as: a instanceof A && (null==a||!(a instanceof B))
    private InstanceOf isNegationOfInstanceOf(Expression expression) {
        Or or;
        Equals equals;
        Negation negation;
        InstanceOf instance;
        return expression != null
               && (or = expression.asInstanceOf(Or.class)) != null
               && or.expressions().size() == 2
               && (equals = or.expressions().get(0).asInstanceOf(Equals.class)) != null
               && equals.lhs().isNullConstant()
               && (negation = or.expressions().get(1).asInstanceOf(Negation.class)) != null
               && (instance = negation.expression().asInstanceOf(InstanceOf.class)) != null
               && instance.expression().equals(equals.rhs()) ? instance : null;
    }

    private List<Expression> components(Expression value) {
        Or or;
        if ((or = value.asInstanceOf(Or.class)) != null) {
            return or.expressions();
        }
        return List.of(value);
    }

    private static void recursivelyAdd(ArrayList<Expression> concat, List<Expression> values) {
        for (Expression value : values) {
            And and;
            if ((and = value.asInstanceOf(And.class)) != null) {
                recursivelyAdd(concat, and.expressions());
            } else {
                concat.add(value);
            }
        }
    }

}
