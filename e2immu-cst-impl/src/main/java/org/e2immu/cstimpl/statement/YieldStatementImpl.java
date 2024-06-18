package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.YieldStatement;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.KeywordImpl;
import org.e2immu.cstimpl.output.SpaceEnum;
import org.e2immu.cstimpl.output.SymbolEnum;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class YieldStatementImpl extends StatementImpl implements YieldStatement {

    private final Expression expression;

    public YieldStatementImpl(Expression expression) {
        this.expression = expression;
    }

    public YieldStatementImpl(List<Comment> comments,
                              Source source,
                              List<AnnotationExpression> annotations,
                              String label,
                              Expression expression) {
        super(comments, source, annotations, expression.complexity(), label);
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YieldStatementImpl that)) return false;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(expression);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification).add(KeywordImpl.YIELD);
        if (!expression.isEmpty()) {
            outputBuilder.add(SpaceEnum.ONE).add(expression.print(qualification));
        }
        outputBuilder.add(SymbolEnum.SEMICOLON);
        return outputBuilder;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return expression.variables(descendMode);
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return expression.typesReferenced();
    }

    @Override
    public Expression expression() {
        return expression;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            expression.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            expression.visit(visitor);
        }
        visitor.afterStatement(this);
    }

    public static class Builder extends StatementImpl.Builder<YieldStatement.Builder> implements YieldStatement.Builder {
        private Expression expression;

        @Override
        public Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public YieldStatement build() {
            return new YieldStatementImpl(comments, source, annotations, label, expression);
        }
    }
}
