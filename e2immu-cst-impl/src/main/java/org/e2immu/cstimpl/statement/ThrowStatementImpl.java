package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.Statement;
import org.e2immu.cstapi.statement.ThrowStatement;
import org.e2immu.cstapi.translate.TranslationMap;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.KeywordImpl;
import org.e2immu.cstimpl.output.SpaceEnum;
import org.e2immu.cstimpl.output.SymbolEnum;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ThrowStatementImpl extends StatementImpl implements ThrowStatement {
    private final Expression expression;

    public ThrowStatementImpl(List<Comment> comments, Source source, List<AnnotationExpression> annotations,
                              String label, Expression expression) {
        super(comments, source, annotations, 1 + expression.complexity(), label);
        this.expression = Objects.requireNonNull(expression);
    }

    public static class Builder extends StatementImpl.Builder<ThrowStatement.Builder> implements ThrowStatement.Builder {
        private Expression expression;

        @Override
        public Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }


        @Override
        public ThrowStatement build() {
            return new ThrowStatementImpl(comments, source, annotations, label, expression);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThrowStatementImpl that = (ThrowStatementImpl) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(expression);
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

    @Override
    public OutputBuilder print(Qualification qualification) {
        return outputBuilder(qualification)
                .add(KeywordImpl.THROW)
                .add(SpaceEnum.ONE).add(expression.print(qualification))
                .add(SymbolEnum.SEMICOLON);
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
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (haveDirectTranslation(direct, this)) return direct;

        Expression tex = expression.translate(translationMap);
        if (tex == expression) return List.of(this);
        return List.of(new ThrowStatementImpl(comments(), source(), annotations(), label(),
                translationMap.translateExpression(expression)));
    }
}
