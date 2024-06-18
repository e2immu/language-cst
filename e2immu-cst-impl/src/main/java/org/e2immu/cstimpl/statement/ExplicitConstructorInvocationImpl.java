package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.ExplicitConstructorInvocation;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.output.TextImpl;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ExplicitConstructorInvocationImpl extends StatementImpl implements ExplicitConstructorInvocation {
    private final boolean isSuper;
    private final MethodInfo methodInfo;
    private final List<Expression> parameterExpressions;

    public ExplicitConstructorInvocationImpl(List<Comment> comments,
                                             Source source, List<AnnotationExpression> annotations,
                                             String label, boolean isSuper, MethodInfo methodInfo,
                                             List<Expression> parameterExpressions) {
        super(comments, source, annotations, 1 + methodInfo.complexity(), label);
        this.isSuper = isSuper;
        this.methodInfo = methodInfo;
        this.parameterExpressions = parameterExpressions;
        assert source == null || "0".equals(source.index());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExplicitConstructorInvocationImpl that)) return false;
        return isSuper() == that.isSuper()
               && Objects.equals(methodInfo, that.methodInfo)
               && Objects.equals(parameterExpressions, that.parameterExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSuper(), methodInfo, parameterExpressions);
    }

    public static class Builder extends StatementImpl.Builder<ExplicitConstructorInvocation.Builder>
            implements ExplicitConstructorInvocation.Builder {
        private boolean isSuper;
        private MethodInfo methodInfo;
        private List<Expression> parameterExpressions;

        @Override
        public Builder setIsSuper(boolean isSuper) {
            this.isSuper = isSuper;
            return this;
        }

        @Override
        public Builder setMethodInfo(MethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }

        @Override
        public Builder setParameterExpressions(List<Expression> parameterExpressions) {
            this.parameterExpressions = parameterExpressions;
            return this;
        }

        @Override
        public ExplicitConstructorInvocation build() {
            return new ExplicitConstructorInvocationImpl(comments, source, annotations, label, isSuper,
                    methodInfo, parameterExpressions);
        }
    }

    @Override
    public boolean isSuper() {
        return isSuper;
    }

    @Override
    public MethodInfo methodInfo() {
        return methodInfo;
    }

    @Override
    public List<Expression> parameterExpressions() {
        return parameterExpressions;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            parameterExpressions.forEach(e -> e.visit(predicate));
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            parameterExpressions.forEach(e -> e.visit(visitor));
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        String name = isSuper ? "super" : "this";
        OutputBuilder outputBuilder = new OutputBuilderImpl().add(new TextImpl(name));
        if (parameterExpressions.isEmpty()) {
            outputBuilder.add(SymbolEnum.OPEN_CLOSE_PARENTHESIS);
        } else {
            outputBuilder.add(SymbolEnum.LEFT_PARENTHESIS)
                    .add(parameterExpressions.stream()
                            .map(expression -> expression.print(qualification))
                            .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)))
                    .add(SymbolEnum.RIGHT_PARENTHESIS);
        }
        return outputBuilder.add(SymbolEnum.SEMICOLON);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return parameterExpressions.stream().flatMap(e -> e.variables(descendMode));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return parameterExpressions.stream().flatMap(Expression::typesReferenced);
    }
}
