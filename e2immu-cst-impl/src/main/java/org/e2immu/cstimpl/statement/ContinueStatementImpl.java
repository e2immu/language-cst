package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.ContinueStatement;
import org.e2immu.cstimpl.output.KeywordImpl;
import org.e2immu.cstimpl.output.SpaceEnum;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.output.TextImpl;

import java.util.List;
import java.util.Objects;

public class ContinueStatementImpl extends BreakOrContinueStatementImpl implements ContinueStatement {
    public ContinueStatementImpl(List<Comment> comments, Source source,
                                 List<AnnotationExpression> annotationExpressions, String label, String goToLabel) {
        super(comments, source, annotationExpressions, label, goToLabel);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ContinueStatement cs && Objects.equals(goToLabel(), cs.goToLabel());
    }

    public static class Builder extends StatementImpl.Builder<ContinueStatement.Builder>
            implements ContinueStatement.Builder {

        private String goToLabel;

        @Override
        public Builder setGoToLabel(String goToLabel) {
            this.goToLabel = goToLabel;
            return this;
        }

        @Override
        public ContinueStatement build() {
            return new ContinueStatementImpl(comments, source, annotations, label, goToLabel);
        }
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification).add(KeywordImpl.CONTINUE);
        if (goToLabel() != null) {
            outputBuilder.add(SpaceEnum.ONE).add(new TextImpl(goToLabel()));
        }
        outputBuilder.add(SymbolEnum.SEMICOLON);
        return outputBuilder;
    }
}
