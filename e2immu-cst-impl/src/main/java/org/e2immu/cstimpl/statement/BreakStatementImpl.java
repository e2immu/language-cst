package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.BreakStatement;
import org.e2immu.cstimpl.output.KeywordImpl;
import org.e2immu.cstimpl.output.SpaceEnum;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.output.TextImpl;

import java.util.List;
import java.util.Objects;

public class BreakStatementImpl extends BreakOrContinueStatementImpl implements BreakStatement {
    public BreakStatementImpl(List<Comment> comments, Source source,
                              List<AnnotationExpression> annotationExpressions, String label, String goToLabel) {
        super(comments, source, annotationExpressions, label, goToLabel);
    }

    public static class Builder extends StatementImpl.Builder<BreakStatement.Builder> implements BreakStatement.Builder {

        private String goToLabel;

        @Override
        public Builder setGoToLabel(String goToLabel) {
            this.goToLabel = goToLabel;
            return this;
        }

        @Override
        public BreakStatement build() {
            return new BreakStatementImpl(comments, source, annotations, label, goToLabel);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BreakStatement bs && Objects.equals(goToLabel(), bs.goToLabel());
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification).add(KeywordImpl.BREAK);
        if (goToLabel() != null) {
            outputBuilder.add(SpaceEnum.ONE).add(new TextImpl(goToLabel()));
        }
        outputBuilder.add(SymbolEnum.SEMICOLON);
        return outputBuilder;
    }
}
