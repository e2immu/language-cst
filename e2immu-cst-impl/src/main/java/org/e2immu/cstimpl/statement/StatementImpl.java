package org.e2immu.cstimpl.statement;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.Block;
import org.e2immu.cstapi.statement.Statement;
import org.e2immu.cstimpl.analysis.PropertyImpl;
import org.e2immu.cstimpl.analysis.ValueImpl;
import org.e2immu.cstimpl.element.ElementImpl;
import org.e2immu.cstimpl.output.OutputBuilderImpl;
import org.e2immu.cstimpl.output.SpaceEnum;
import org.e2immu.cstimpl.output.SymbolEnum;
import org.e2immu.cstimpl.output.TextImpl;

import java.util.List;

public abstract class StatementImpl extends ElementImpl implements Statement {

    private final List<Comment> comments;
    private final List<AnnotationExpression> annotations;
    private final Source source;
    private final int complexity;
    private final String label;

    protected StatementImpl(List<Comment> comments,
                            Source source,
                            List<AnnotationExpression> annotations,
                            int complexity,
                            String label) {
        this.complexity = complexity;
        this.source = source;
        this.annotations = annotations;
        this.comments = comments;
        this.label = label;
    }

    protected StatementImpl() {
        this(List.of(), null, List.of(), 1, null);
    }

    protected OutputBuilder outputBuilder(Qualification qualification) {
        OutputBuilder ob = new OutputBuilderImpl();
        if (!comments.isEmpty()) {
            ob.add(comments.stream()
                    .map(c -> c.print(qualification)).collect(OutputBuilderImpl.joining(SpaceEnum.NEWLINE)));
            ob.add(SpaceEnum.NEWLINE);
        }
        if (!annotations.isEmpty()) {
            ob.add(annotations().stream()
                    .map(ae -> ae.print(qualification)).collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)));
            ob.add(SpaceEnum.NEWLINE);
        }
        if (label != null) {
            ob.add(new TextImpl(label)).add(SymbolEnum.COLON_LABEL).add(SpaceEnum.ONE_IS_NICE_EASY_SPLIT);
            ob.add(SpaceEnum.ONE);
        }
        return ob;
    }

    @Override
    public Source source() {
        return source;
    }

    @Override
    public List<AnnotationExpression> annotations() {
        return annotations;
    }

    @Override
    public List<Comment> comments() {
        return comments;
    }

    @Override
    public int complexity() {
        return complexity;
    }

    @Override
    public String label() {
        return label;
    }

    @SuppressWarnings("unchecked")
    public static abstract class Builder<B extends Statement.Builder<?>> extends ElementImpl.Builder<B> implements Statement.Builder<B> {
        protected String label;

        @Fluent
        public B setLabel(String label) {
            this.label = label;
            return (B) this;
        }
    }

    protected boolean haveDirectTranslation(List<Statement> resultOfTranslation, Statement statement) {
        return resultOfTranslation.size() != 1 || resultOfTranslation.get(0) != statement;
    }

    protected Block ensureBlock(List<Statement> resultOfTranslation) {
        if (resultOfTranslation.size() == 1 && resultOfTranslation.get(0) instanceof Block block) {
            return block;
        }
        return new BlockImpl.Builder().addStatements(resultOfTranslation).build();
    }

    @Override
    public boolean alwaysEscapes() {
        return analysis().getOrDefault(PropertyImpl.ALWAYS_ESCAPES, ValueImpl.BoolImpl.FALSE).isTrue();
    }
}
