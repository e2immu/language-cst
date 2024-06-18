package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.*;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.output.element.Symbol;
import org.e2immu.cstapi.output.element.Text;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.expression.util.InternalCompareToException;
import org.e2immu.cstimpl.expression.util.PrecedenceEnum;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.output.TextImpl;
import org.e2immu.cstimpl.util.IntUtil;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GreaterThanZeroImpl extends ExpressionImpl implements GreaterThanZero {
    private final Expression expression;
    private final boolean allowEquals;
    private final ParameterizedType booleanPt;

    public GreaterThanZeroImpl(Runtime runtime, Expression expression, boolean allowEquals) {
        this(runtime.booleanParameterizedType(), expression, allowEquals);
    }

    public GreaterThanZeroImpl(ParameterizedType booleanPt, Expression expression, boolean allowEquals) {
        super(1 + expression.complexity());
        this.expression = expression;
        this.allowEquals = allowEquals;
        this.booleanPt = booleanPt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GreaterThanZeroImpl that)) return false;
        return allowEquals == that.allowEquals && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, allowEquals);
    }

    @Override
    public Expression expression() {
        return expression;
    }

    @Override
    public boolean allowEquals() {
        return allowEquals;
    }

    public record XBImpl(Expression x, double b, boolean lessThan) implements XB {
    }

    @Override
    public XB extract(Runtime runtime) {
        Sum sumValue = expression.asInstanceOf(Sum.class);
        if (sumValue != null) {
            Double d = sumValue.numericPartOfLhs();
            if (d != null) {
                Expression v = sumValue.nonNumericPartOfLhs(runtime);
                Expression x;
                boolean lessThan;
                double b;
                if (v instanceof Negation ne) {
                    x = ne.expression();
                    lessThan = true;
                    if (IntUtil.isMathematicalInteger(d)) {
                        assert !allowEquals : "By convention, we store x < 4 rather than x <= 3";
                        b = d - 1;
                    } else {
                        b = d;
                    }
                } else {
                    x = v;
                    lessThan = false;
                    b = -d;
                }
                return new XBImpl(x, b, lessThan);
            }
        }
        Expression x;
        boolean lessThan;
        double d;
        if (expression instanceof Negation ne) {
            x = ne.expression();
            lessThan = true;
            if (!allowEquals && x.parameterizedType().isMathematicallyInteger()) {
                d = -1;
            } else {
                d = 0;
            }
        } else {
            x = expression;
            lessThan = false;
            d = 0;
        }
        return new XBImpl(x, d, lessThan);
    }

    @Override
    public ParameterizedType parameterizedType() {
        return booleanPt;
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.RELATIONAL;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_GEQ0;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        if (expression instanceof BinaryOperator binary) {
            return -BinaryOperatorImpl.compareBinaryToGt0(binary, this);
        }
        if (!(expression instanceof GreaterThanZero)) throw new InternalCompareToException();

        int c = BinaryOperatorImpl.compareVariables(this, expression);
        if (c != 0) return c;
        return expression.compareTo(((GreaterThanZero) expression).expression());
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression translatedExpression = expression.translate(translationMap);
        if (translatedExpression == expression) return this;
        return new GreaterThanZeroImpl(booleanPt, translatedExpression, allowEquals);
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            expression.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(this)) {
            expression.visit(visitor);
        }
        visitor.afterExpression(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        Symbol gt = SymbolEnum.binaryOperator(allowEquals ? ">=" : ">");
        Symbol lt = SymbolEnum.binaryOperator(allowEquals ? "<=" : "<");
        if (expression instanceof Sum sum) {
            if (sum.lhs() instanceof Numeric ln) {
                if (ln.doubleValue() < 0) {
                    // -1 -a >= 0 will be written as a <= -1
                    if (sum.rhs() instanceof Negation neg) {
                        return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), neg.expression()))
                                .add(lt).add(sum.lhs().print(qualification));
                    }
                    // -1 + a >= 0 will be written as a >= 1
                    Text negNumber = new TextImpl(DoubleConstantImpl.formatNumber(-ln.doubleValue(), ln.getClass()));
                    return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), sum.rhs()))
                            .add(gt).add(negNumber);
                } else if (sum.rhs() instanceof Negation neg) {
                    // 1 + -a >= 0 will be written as a <= 1
                    return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), neg.expression()))
                            .add(lt).add(sum.lhs().print(qualification));
                }
            }
            // according to sorting, the rhs cannot be numeric

            // -x + a >= 0 will be written as a >= x
            if (sum.lhs() instanceof Negation neg && !(sum.rhs() instanceof Negation)) {
                return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), sum.rhs()))
                        .add(gt).add(outputInParenthesis(qualification, precedence(), neg.expression()));
            }
            // a + -x >= 0 will be written as a >= x
            if (sum.rhs() instanceof Negation neg && !(sum.lhs() instanceof Negation)) {
                return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), sum.lhs()))
                        .add(gt).add(outputInParenthesis(qualification, precedence(), neg.expression()));
            }
        } else if (expression instanceof Negation neg) {
            return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), neg.expression()))
                    .add(lt).add(new TextImpl("0"));
        }
        return new OutputBuilderImpl().add(outputInParenthesis(qualification, precedence(), expression))
                .add(gt).add(new TextImpl("0"));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return expression.variables(descendMode);
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return expression.typesReferenced();
    }
}
