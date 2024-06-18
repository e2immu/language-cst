package org.e2immu.cstimpl.runtime;

import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.info.ComputeMethodOverrides;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.runtime.Configuration;
import org.e2immu.cstapi.runtime.Eval;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.element.E2ImmuAnnotationsImpl;
import org.e2immu.cstimpl.info.ComputeMethodOverridesImpl;

import java.util.List;
import java.util.stream.Stream;

public class RuntimeImpl extends FactoryImpl implements Runtime {
    private final Eval eval = new EvalImpl(this);
    private final E2ImmuAnnotationsImpl e2ImmuAnnotations = new E2ImmuAnnotationsImpl();

    @Override
    public Configuration configuration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression inlineConditional(Expression condition,
                                        Expression ifTrue,
                                        Expression ifFalse,
                                        Variable myself,
                                        boolean modifying) {
        return eval.inlineConditional(condition, ifTrue, ifFalse, myself, modifying);
    }

    @Override
    public Expression product(Expression lhs, Expression rhs) {
        return eval.product(lhs, rhs);
    }

    @Override
    public Expression less(Expression lhs, Expression rhs, boolean allowEquals) {
        return eval.less(lhs, rhs, allowEquals);
    }

    @Override
    public Expression wrapInProduct(Expression[] translated, int length) {
        return null;
    }

    @Override
    public Expression wrapInSum(Expression[] translated, int length) {
        return null;
    }

    @Override
    public Expression sum(Expression lhs, Expression rhs) {
        return eval.sum(lhs, rhs);
    }

    @Override
    public Expression negate(Expression expression) {
        return eval.negate(expression);
    }

    @Override
    public Expression remainder(Expression lhs, Expression rhs) {
        return eval.remainder(lhs, rhs);
    }

    @Override
    public Expression equals(Expression lhs, Expression rhs) {
        return eval.equals(lhs, rhs);
    }

    @Override
    public Expression greater(Expression lhs, Expression rhs, boolean allowEquals) {
        return eval.greater(lhs, rhs, allowEquals);
    }

    @Override
    public Expression greaterThanZero(Expression expression) {
        return eval.greaterThanZero(expression);
    }

    @Override
    public Expression or(List<Expression> expressions) {
        return eval.or(expressions);
    }

    @Override
    public Expression or(Expression... expressions) {
        return eval.or(expressions);
    }

    @Override
    public Expression and(Expression... expressions) {
        return eval.and(expressions);
    }

    @Override
    public Expression and(List<Expression> expressions) {
        return eval.and(expressions);
    }

    @Override
    public Expression divide(Expression lhs, Expression rhs) {
        return eval.divide(lhs, rhs);
    }

    @Override
    public boolean isNotNull0(Expression expression) {
        return eval.isNotNull0(expression);
    }

    @Override
    public int limitOnComplexity() {
        return eval.limitOnComplexity();
    }

    @Override
    public Stream<Expression> expandTerms(Expression expression, boolean negate) {
        return eval.expandTerms(expression, negate);
    }

    @Override
    public Stream<Expression> expandFactors(Expression expression) {
        return eval.expandFactors(expression);
    }

    @Override
    public TypeInfo getFullyQualified(String name, boolean complain) {
        throw new UnsupportedOperationException(); // FIXME
    }

    @Override
    public TypeInfo syntheticFunctionalType(int inputParameters, boolean hasReturnValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<AnnotationExpression> e2immuAnnotations() {
        return e2ImmuAnnotations.streamTypes();
    }

    @Override
    public String e2aAbsent() {
        return E2ImmuAnnotationsImpl.ABSENT;
    }

    @Override
    public AnnotationExpression e2immuAnnotation(String fullyQualifiedName) {
        return e2ImmuAnnotations.get(fullyQualifiedName);
    }

    @Override
    public String e2aContract() {
        return E2ImmuAnnotationsImpl.CONTRACT;
    }

    @Override
    public String e2aContent() {
        return E2ImmuAnnotationsImpl.CONTENT;
    }

    @Override
    public String e2aImplied() {
        return E2ImmuAnnotationsImpl.IMPLIED;
    }

    @Override
    public String e2aHiddenContent() {
        return E2ImmuAnnotationsImpl.HIDDEN_CONTENT;
    }

    @Override
    public String e2aValue() {
        return E2ImmuAnnotationsImpl.VALUE;
    }

    @Override
    public String e2aPar() {
        return E2ImmuAnnotationsImpl.PAR;
    }

    @Override
    public String e2aSeq() {
        return E2ImmuAnnotationsImpl.SEQ;
    }

    @Override
    public String e2aMulti() {
        return E2ImmuAnnotationsImpl.MULTI;
    }

    @Override
    public String e2aAfter() {
        return E2ImmuAnnotationsImpl.AFTER;
    }

    @Override
    public String e2aBefore() {
        return E2ImmuAnnotationsImpl.BEFORE;
    }

    @Override
    public String e2aConstruction() {
        return E2ImmuAnnotationsImpl.CONSTRUCTION;
    }

    @Override
    public String e2aInconclusive() {
        return E2ImmuAnnotationsImpl.INCONCLUSIVE;
    }

    @Override
    public String e2aHcParameters() {
        return E2ImmuAnnotationsImpl.HC_PARAMETERS;
    }

    @Override
    public ComputeMethodOverrides computeMethodOverrides() {
        return new ComputeMethodOverridesImpl(this);
    }
}
