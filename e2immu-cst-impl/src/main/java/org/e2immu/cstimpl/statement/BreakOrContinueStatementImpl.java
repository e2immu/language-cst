package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.statement.BreakOrContinueStatement;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BreakOrContinueStatementImpl extends StatementImpl implements BreakOrContinueStatement {
    private final String goToLabel;

    protected BreakOrContinueStatementImpl(List<Comment> comments, Source source,
                                           List<AnnotationExpression> annotationExpressions,
                                       String label, String goToLabel) {
        super(comments, source, annotationExpressions, 1, label);
        this.goToLabel = goToLabel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(goToLabel);
    }

    @Override
    public String goToLabel() {
        return goToLabel;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        predicate.test(this);
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.beforeStatement(this);
        visitor.afterStatement(this);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.empty();
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.empty();
    }
}
