package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.AssertStatement;
import org.e2immu.cstapi.statement.Statement;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.KeywordImpl;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SpaceEnum;
import org.e2immu.cstimpl.output.SymbolEnum;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AssertStatementImpl extends StatementImpl implements AssertStatement {
    private final Expression message;
    private final Expression expression;

    public AssertStatementImpl(List<Comment> comments, Source source, List<AnnotationExpression> annotations,
                               String label, Expression expression, Expression message) {
        super(comments, source, annotations, 1 + message.complexity() + expression.complexity(), label);
        this.expression = Objects.requireNonNull(expression);
        this.message = Objects.requireNonNull(message); // use empty expression if no message
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssertStatementImpl that)) return false;
        return Objects.equals(message, that.message) && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, expression);
    }

    public static class Builder extends StatementImpl.Builder<AssertStatement.Builder> implements AssertStatement.Builder {
        private Expression message;
        private Expression expression;

        @Override
        public AssertStatement.Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public AssertStatement.Builder setMessage(Expression message) {
            this.message = message;
            return this;
        }

        @Override
        public AssertStatement build() {
            return new AssertStatementImpl(comments, source, annotations, label, expression, message);
        }
    }

    @Override
    public Expression message() {
        return message;
    }

    @Override
    public Expression expression() {
        return expression;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            expression.visit(predicate);
            message.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            expression.visit(visitor);
            message.visit(visitor);
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return outputBuilder(qualification)
                .add(KeywordImpl.ASSERT)
                .add(SpaceEnum.ONE)
                .add(expression.print(qualification))
                .add(message.isEmpty() ? new OutputBuilderImpl()
                        : new OutputBuilderImpl().add(SymbolEnum.COMMA).add(message.print(qualification)))
                .add(SymbolEnum.SEMICOLON);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(expression.variables(descendMode), message.variables(descendMode));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.concat(expression.typesReferenced(), message.typesReferenced());
    }

    @Override
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (haveDirectTranslation(direct, this)) return direct;
        Expression tex = expression.translate(translationMap);
        Expression msg = message.translate(translationMap);
        if (tex == expression && msg == message) return List.of(this);
        return List.of(new AssertStatementImpl(comments(), source(), annotations(), label(), tex, msg));
    }
}
