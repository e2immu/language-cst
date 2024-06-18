package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.expression.VariableExpression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.DependentVariable;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.FieldReference;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.element.ElementImpl;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.expression.util.InternalCompareToException;
import org.e2immu.cstimpl.expression.util.PrecedenceEnum;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.TextImpl;
import org.e2immu.cstimpl.variable.DependentVariableImpl;
import org.e2immu.cstimpl.variable.FieldReferenceImpl;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class VariableExpressionImpl extends ExpressionImpl implements VariableExpression {

    private final Variable variable;
    private final Suffix suffix;

    public VariableExpressionImpl(Variable variable) {
        this(null, List.of(), variable, null);
    }

    public VariableExpressionImpl(Source source, List<Comment> comments, Variable variable, Suffix suffix) {
        super(comments, source, variable.complexity());
        this.variable = variable;
        this.suffix = suffix;
    }

    @Override
    public Suffix suffix() {
        return suffix;
    }

    @Override
    public Variable variable() {
        return variable;
    }

    public static class Builder extends ElementImpl.Builder<VariableExpression.Builder> implements VariableExpression.Builder {
        private Variable variable;
        private Suffix suffix;

        @Override
        public VariableExpression.Builder setVariable(Variable variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public VariableExpression.Builder setSuffix(Suffix suffix) {
            this.suffix = suffix;
            return this;
        }

        @Override
        public VariableExpression build() {
            return new VariableExpressionImpl(source, comments, variable, suffix);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableExpressionImpl that = (VariableExpressionImpl) o;
        return Objects.equals(variable, that.variable) && Objects.equals(suffix, that.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, suffix);
    }

    @Override
    public VariableExpression withSuffix(Suffix suffix) {
        return new VariableExpressionImpl(source(), comments(), variable, suffix);
    }

    @Override
    public ParameterizedType parameterizedType() {
        return variable.parameterizedType();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            variable.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(this)) {
            variable.visit(visitor);
        }
        visitor.afterExpression(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = new OutputBuilderImpl();
        // if (variable instanceof FieldReference fr && !fr.isStatic()) {
        //    if (!fr.isDefaultScope()) {
        // outputBuilder.add(outputInParenthesis(qualification, precedence(), scopeValue)).add(SymbolEnum.DOT);
        //    }
        //    outputBuilder.add(new TextImpl(fr.fieldInfo().name()));
        //} else if (variable instanceof DependentVariable dv) { TODO NYI
        //    outputBuilder.add(outputInParenthesis(qualification, precedence(), scopeValue))
        //            .add(SymbolEnum.LEFT_BRACKET).add(indexValue.output(qualification)).add(Symbol.RIGHT_BRACKET);
        //} else {
        outputBuilder.add(variable.print(qualification));
        // }
        if(suffix != null) outputBuilder.add(suffix.print());
        return outputBuilder;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return variable.variables(descendMode);
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return variable.typesReferenced();
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.TOP;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_VARIABLE;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        VariableExpression ve;
        if ((ve = expression.asInstanceOf(VariableExpression.class)) != null) {
            return variable.fullyQualifiedName().compareTo(ve.variable().fullyQualifiedName());
        }
        throw new InternalCompareToException();
    }

    public record VariableFieldSuffix(int statementTime,
                                      String latestAssignment) implements VariableExpression.VariableField {

        @Override
        public OutputBuilder print() {
            OutputBuilder outputBuilder = new OutputBuilderImpl();
            if (latestAssignment != null) outputBuilder.add(new TextImpl("$" + latestAssignment));
            outputBuilder.add(new TextImpl("$" + statementTime));
            return outputBuilder;
        }
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        // see explanation in TranslationMapImpl for the order of translation.
        Expression translated1 = translationMap.translateExpression(this);
        if (translated1 != this) {
            return translated1;
        }
        Expression translated2 = translationMap.translateVariableExpressionNullIfNotTranslated(variable);
        if (translated2 != null) {
            return translated2;
        }
        Variable translated3 = translationMap.translateVariable( variable);
        if (translated3 != variable) {
            return new VariableExpressionImpl(translated3);
        }
        if (translationMap.recurseIntoScopeVariables()) {
            if (variable instanceof FieldReference fr) {
                Expression translated = fr.scope().translate(translationMap);
                if (translated != fr.scope()) {
                    FieldReference newFr = new FieldReferenceImpl(fr.fieldInfo(), translated, null,
                            fr.parameterizedType());
                  //  if (translated.isDelayed()) {
                  //      int statementTime = translated instanceof DelayedVariableExpression dve ? dve.statementTime : 0;
                  //     return DelayedVariableExpression.forField(newFr, statementTime, translated.causesOfDelay());
                  //  }
                    return new VariableExpressionImpl(source(), comments(), newFr, suffix);
                }
               // if (!translated.equals(scopeValue)) {
                    // change the scopeValue to the translated one (see e.g. Basics_21.copy(),
                    // which translates using RemoveSuffixesTranslationMap)
                //    return new VariableExpression(identifier, fr, suffix, translated, null);
                //}
            } else if (variable instanceof DependentVariable dv) {
                Expression translatedScope = dv.arrayExpression().translate( translationMap);
                Expression translatedIndex = dv.indexExpression().translate( translationMap);
                if (translatedScope != dv.arrayExpression() || translatedIndex != dv.indexExpression()) {
                    Variable arrayVariable = DependentVariableImpl.makeVariable(translatedScope,
                            DependentVariableImpl.ARRAY_VARIABLE);
                    assert arrayVariable != null;
                    Variable indexVariable = DependentVariableImpl.makeVariable(translatedIndex,
                            DependentVariableImpl.INDEX_VARIABLE);
                    DependentVariable newDv = new DependentVariableImpl( translatedScope,
                            arrayVariable, translatedIndex, indexVariable, dv.parameterizedType());
                  //  if (newDv.causesOfDelay().isDelayed()) {
                  //      return DelayedVariableExpression.forDependentVariable(newDv, newDv.causesOfDelay());
                  //  }
                    return new VariableExpressionImpl(source(), comments(), newDv, suffix);
                }
            }
        }
        return this;
    }
}
