package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.MethodReference;
import org.e2immu.cstapi.expression.Precedence;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.element.ElementImpl;
import org.e2immu.cstimpl.expression.util.ExpressionComparator;
import org.e2immu.cstimpl.expression.util.InternalCompareToException;
import org.e2immu.cstimpl.expression.util.PrecedenceEnum;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.output.TextImpl;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MethodReferenceImpl extends ExpressionImpl implements MethodReference {
    private final ParameterizedType parameterizedType;
    private final MethodInfo methodInfo;
    private final Expression scope;

    public MethodReferenceImpl(List<Comment> comments, Source source,
                               ParameterizedType parameterizedType, MethodInfo methodInfo, Expression scope) {
        super(comments, source, 1 + scope.complexity());
        this.parameterizedType = parameterizedType;
        this.methodInfo = methodInfo;
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodReferenceImpl that)) return false;
        return Objects.equals(methodInfo, that.methodInfo) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodInfo, scope);
    }

    public static class Builder extends ElementImpl.Builder<MethodReference.Builder> implements MethodReference.Builder {
        private ParameterizedType parameterizedType;
        private MethodInfo methodInfo;
        private Expression scope;

        @Override
        public Builder setScope(Expression expression) {
            this.scope = expression;
            return this;
        }

        @Override
        public Builder setMethod(MethodInfo method) {
            this.methodInfo = method;
            return this;
        }

        @Override
        public Builder setConcreteReturnType(ParameterizedType parameterizedType) {
            this.parameterizedType = parameterizedType;
            return this;
        }

        @Override
        public MethodReference build() {
            return new MethodReferenceImpl(comments, source, parameterizedType, methodInfo, scope);
        }
    }

    @Override
    public ParameterizedType parameterizedType() {
        return parameterizedType;
    }

    @Override
    public Precedence precedence() {
        return PrecedenceEnum.ARRAY_ACCESS;
    }

    @Override
    public int order() {
        return ExpressionComparator.ORDER_METHOD_REFERENCE;
    }

    @Override
    public int internalCompareTo(Expression expression) {
        if (expression instanceof MethodReference mr) {
            int c = methodInfo.fullyQualifiedName().compareTo(mr.methodInfo().fullyQualifiedName());
            if (c == 0) return scope.compareTo(mr.scope());
            return c;
        }
        throw new InternalCompareToException();
    }

    @Override
    public Expression scope() {
        return scope;
    }

    @Override
    public MethodInfo methodInfo() {
        return methodInfo;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            scope.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeExpression(this)) {
            scope.visit(visitor);
        }
        visitor.afterExpression(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        String methodName = methodInfo.isConstructor() ? "new" : methodInfo.name();
        return new OutputBuilderImpl().add(scope.print(qualification)).add(SymbolEnum.DOUBLE_COLON).add(new TextImpl(methodName));
    }

    @Override
    public Expression translate(TranslationMap translationMap) {
        Expression translated = translationMap.translateExpression(this);
        if (translated != this) return translated;

        Expression translatedScope = scope.translate(translationMap);
        ParameterizedType transType = translationMap.translateType(parameterizedType);
        if (translatedScope == scope && transType == parameterizedType) return this;
        return new MethodReferenceImpl(comments(), source(), transType, methodInfo, translatedScope);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return scope.variables(descendMode);
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return scope.typesReferenced();
    }
}
