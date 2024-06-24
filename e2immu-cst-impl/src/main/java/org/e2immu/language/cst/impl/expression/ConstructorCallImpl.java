package org.e2immu.language.cst.impl.expression;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.ArrayInitializer;
import org.e2immu.language.cst.api.expression.ConstructorCall;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.Precedence;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.Diamond;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.language.cst.impl.expression.util.ExpressionComparator;
import org.e2immu.language.cst.impl.expression.util.InternalCompareToException;
import org.e2immu.language.cst.impl.expression.util.PrecedenceEnum;
import org.e2immu.language.cst.impl.output.KeywordImpl;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SpaceEnum;
import org.e2immu.language.cst.impl.output.SymbolEnum;
import org.e2immu.language.cst.impl.util.ListUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConstructorCallImpl extends ExpressionImpl implements ConstructorCall {
    private final MethodInfo constructor;
    private final Diamond diamond;
    private final Expression object;
    private final List<Expression> parameterExpressions;
    private final ArrayInitializer arrayInitializer;
    private final TypeInfo anonymousClass;
    private final ParameterizedType concreteReturnType;

    public ConstructorCallImpl(List<Comment> comments, Source source, MethodInfo constructor,
                               ParameterizedType concreteReturnType,
                               Diamond diamond, Expression object, List<Expression> expressions,
                               ArrayInitializer arrayInitializer, TypeInfo anonymousClass) {
        super(comments, source, 1 + (object == null ? 0 : object.complexity())
                                + expressions.stream().mapToInt(Expression::complexity).sum());
        assert constructor != null || anonymousClass != null;
        this.constructor = constructor;
        this.diamond = Objects.requireNonNull(diamond);
        this.object = object;
        parameterExpressions = expressions;
        this.arrayInitializer = arrayInitializer;
        this.anonymousClass = anonymousClass;
        this.concreteReturnType = Objects.requireNonNull(concreteReturnType);
    }

    public static class Builder extends ElementImpl.Builder<ConstructorCall.Builder> implements ConstructorCall.Builder {
        private MethodInfo constructor;
        private Diamond diamond;
        private Expression object;
        private List<Expression> parameterExpressions;
        private ArrayInitializer arrayInitializer;
        private TypeInfo anonymousClass;
        private ParameterizedType concreteReturnType;

        @Override
        public ConstructorCall build() {
            return new ConstructorCallImpl(comments, source, constructor, concreteReturnType, diamond, object,
                    List.copyOf(parameterExpressions), arrayInitializer, anonymousClass);
        }

        @Override
        public Builder setObject(Expression object) {
            this.object = object;
            return this;
        }

        @Override
        public Builder setDiamond(Diamond diamond) {
            this.diamond = diamond;
            return this;
        }

        @Override
        public Builder setConstructor(MethodInfo constructor) {
            this.constructor = constructor;
            return this;
        }

        @Override
        public Builder setAnonymousClass(TypeInfo anonymousClass) {
            this.anonymousClass = anonymousClass;
            return this;
        }

        @Override
        public Builder setArrayInitializer(ArrayInitializer arrayInitializer) {
            this.arrayInitializer = arrayInitializer;
            return this;
        }

        @Override
        public Builder setParameterExpressions(List<Expression> expressions) {
            this.parameterExpressions = expressions;
            return this;
        }

        @Override
        public Builder setConcreteReturnType(ParameterizedType returnType) {
            this.concreteReturnType = returnType;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstructorCallImpl that)) return false;
        return Objects.equals(constructor, that.constructor)
               && Objects.equals(diamond, that.diamond)
               && Objects.equals(object, that.object)
               && Objects.equals(parameterExpressions, that.parameterExpressions)
               && Objects.equals(arrayInitializer, that.arrayInitializer)
               && Objects.equals(anonymousClass, that.anonymousClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constructor, diamond, object, parameterExpressions, arrayInitializer, anonymousClass);
    }

    @Override
    public MethodInfo constructor() {
        return constructor;
    }

    @Override
    public Expression object() {
        return object;
    }

    @Override
    public List<Expression> parameterExpressions() {
        return parameterExpressions;
    }

    @Override
    public TypeInfo anonymousClass() {
        return anonymousClass;
    }

    @Override
    public ArrayInitializer arrayInitializer() {
        return arrayInitializer;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return concreteReturnType;
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.UNARY;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_NEW_INSTANCE;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        if (expression instanceof ConstructorCall cc) {
            int c = constructor.fullyQualifiedName().compareTo(cc.constructor().fullyQualifiedName());
            if (c != 0) return c;
            int d = ListUtil.compare(parameterExpressions, cc.parameterExpressions());
            if (d != 0) return d;
            if (object == null && cc.object() != null) return -1;
            if (object != null && cc.object() == null) return 1;
            if (object != null) {
                return object.compareTo(cc.object());
            }
            return 0;
        }
        throw new InternalCompareToException();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            if (object != null) object.visit(predicate);
            parameterExpressions.forEach(p -> p.visit(predicate));
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(this)) {
            if (object != null) object.visit(visitor);
            parameterExpressions.forEach(p -> p.visit(visitor));
        }
        visitor.afterExpression(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = new OutputBuilderImpl();
        if (object != null) {
            outputBuilder.add(outputInParenthesis(qualification, precedence(), object));
            outputBuilder.add(SymbolEnum.DOT);
        }
        if (constructor != null || anonymousClass != null) {
            outputBuilder.add(KeywordImpl.NEW).add(SpaceEnum.ONE)
                    .add(concreteReturnType.copyWithoutArrays().print(qualification, false, diamond));
            //      if (arrayInitializer == null) {
            if (concreteReturnType.arrays() > 0) {
                for (int i = 0; i < concreteReturnType.arrays(); i++) {
                    if (i < parameterExpressions.size()) {
                        outputBuilder.add(SymbolEnum.LEFT_BRACKET);
                        Expression size = parameterExpressions.get(i);
                        if (!(size.isEmpty())) {
                            outputBuilder.add(size.print(qualification));
                        }
                        outputBuilder.add(SymbolEnum.RIGHT_BRACKET);
                    } else {
                        outputBuilder.add(SymbolEnum.OPEN_CLOSE_BRACKETS);
                    }
                }
            } else {
                if (parameterExpressions.isEmpty()) {
                    outputBuilder.add(SymbolEnum.OPEN_CLOSE_PARENTHESIS);
                } else {
                    outputBuilder
                            .add(SymbolEnum.LEFT_PARENTHESIS)
                            .add(parameterExpressions.stream().map(expression -> expression.print(qualification))
                                    .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)))
                            .add(SymbolEnum.RIGHT_PARENTHESIS);
                }
            }
            //    }
        }
        if (anonymousClass != null) {
            outputBuilder.add(anonymousClass.print(qualification, true));
        }
        if (arrayInitializer != null) {
            outputBuilder.add(arrayInitializer.print(qualification));
        }
        return outputBuilder;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.empty();
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.empty();
    }

    @Override
    public ConstructorCall withParameterExpressions(List<Expression> newParameterExpressions) {
        return null;
    }

    @Override
    public Diamond diamond() {
        return diamond;
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression translatedObject = object == null ? null : translationMap.translateExpression(object);
        ParameterizedType translatedType = translationMap.translateType(this.parameterizedType());
        List<Expression> translatedParameterExpressions = parameterExpressions.isEmpty() ? parameterExpressions
                : parameterExpressions.stream()
                .map(e -> e.translate(translationMap))
                .filter(e -> !e.isEmpty())
                .collect(translationMap.toList(parameterExpressions));
        ArrayInitializer translatedInitializer = arrayInitializer == null ? null :
                (ArrayInitializer) arrayInitializer.translate(translationMap);
        if (translatedObject == object
            && translatedType == this.parameterizedType()
            && translatedParameterExpressions == this.parameterExpressions
            && translatedInitializer == arrayInitializer) {
            return this;
        }
        return new ConstructorCallImpl(comments(), source(),
                constructor,
                translatedType,
                diamond,
                object,
                translatedParameterExpressions,
                translatedInitializer, anonymousClass);
    }
}
