package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.Precedence;
import org.e2immu.language.cst.api.expression.VariableExpression;
import org.e2immu.language.cst.api.info.FieldInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.*;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.language.cst.impl.expression.util.ExpressionComparator;
import org.e2immu.language.cst.impl.expression.util.InternalCompareToException;
import org.e2immu.language.cst.impl.expression.util.PrecedenceEnum;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.TextImpl;
import org.e2immu.language.cst.impl.variable.DependentVariableImpl;
import org.e2immu.language.cst.impl.variable.FieldReferenceImpl;
import org.e2immu.language.cst.impl.variable.ThisImpl;

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
        if (suffix != null) outputBuilder.add(suffix.print());
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
        Variable translated3 = translationMap.translateVariable(variable);
        if (translated3 != variable) {
            if (variable instanceof LocalVariable from && from.assignmentExpression() != null
                && translated3 instanceof LocalVariable to && to.assignmentExpression() == null) {
                Expression te = from.assignmentExpression().translate(translationMap);
                return new VariableExpressionImpl(to.withAssignmentExpression(te));
            }
            return new VariableExpressionImpl(translated3);
        }


        if (variable instanceof FieldReference fr) {
            Expression translated = fr.scope().translate(translationMap);
            FieldInfo newField = translationMap.translateFieldInfo(fr.fieldInfo());
            if (translated != fr.scope() || newField != fr.fieldInfo()) {
                FieldReference newFr = new FieldReferenceImpl(newField, translated, null,
                        fr.parameterizedType());
                return new VariableExpressionImpl(source(), comments(), newFr, suffix);
            }
        } else if (variable instanceof DependentVariable dv) {
            Expression translatedScope = dv.arrayExpression().translate(translationMap);
            Expression translatedIndex = dv.indexExpression().translate(translationMap);
            if (translatedScope != dv.arrayExpression() || translatedIndex != dv.indexExpression()) {
                Variable arrayVariable = DependentVariableImpl.makeVariable(translatedScope,
                        DependentVariableImpl.ARRAY_VARIABLE);
                assert arrayVariable != null;
                Variable indexVariable = DependentVariableImpl.makeVariable(translatedIndex,
                        DependentVariableImpl.INDEX_VARIABLE);
                DependentVariable newDv = new DependentVariableImpl(translatedScope,
                        arrayVariable, translatedIndex, indexVariable, dv.parameterizedType());
                return new VariableExpressionImpl(source(), comments(), newDv, suffix);
            }
        } else if (variable instanceof This thisVar) {
            ParameterizedType thisVarPt = thisVar.parameterizedType();
            ParameterizedType translatedType = translationMap.translateType(thisVarPt);
            TypeInfo tExplicitly;
            if (thisVar.explicitlyWriteType() == null) {
                tExplicitly = null;
            } else {
                ParameterizedType explicitlyPt = thisVar.explicitlyWriteType().asSimpleParameterizedType();
                ParameterizedType tExplicitlyPt = translationMap.translateType(explicitlyPt);
                tExplicitly = tExplicitlyPt.typeInfo();
            }
            if (translatedType != thisVarPt || !Objects.equals(thisVar.explicitlyWriteType(), tExplicitly)) {
                This newThisVar = new ThisImpl(translatedType, tExplicitly, thisVar.writeSuper());
                return new VariableExpressionImpl(source(), comments(), newThisVar, suffix);
            }
        }

        return this;
    }

    @Override
    public boolean isNumeric() {
        return parameterizedType().isNumeric();
    }

    @Override
    public VariableExpression withSource(Source newSource) {
        return new VariableExpressionImpl(newSource, comments(), variable, suffix);
    }
}
