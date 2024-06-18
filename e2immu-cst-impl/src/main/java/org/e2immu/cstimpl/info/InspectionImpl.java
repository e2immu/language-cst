package org.e2immu.cstimpl.info;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.info.Access;
import org.e2immu.cstapi.info.Info;

import java.util.ArrayList;
import java.util.List;

public abstract class InspectionImpl implements Inspection {
    private final Access access;
    private final List<Comment> comments;
    private final Source source;
    private final boolean synthetic;
    private final List<AnnotationExpression> annotations;

    public enum AccessEnum implements Access {
        PRIVATE(0), PACKAGE(1), PROTECTED(2), PUBLIC(3);

        private final int level;

        AccessEnum(int level) {
            this.level = level;
        }

        public Access combine(Access other) {
            if (level < other.level()) return this;
            return other;
        }

        @Override
        public boolean isPublic() {
            return this == PUBLIC;
        }

        @Override
        public boolean isPackage() {
            return this == PACKAGE;
        }

        @Override
        public boolean isPrivate() {
            return this == PRIVATE;
        }

        @Override
        public boolean isProtected() {
            return this == PROTECTED;
        }

        @Override
        public int level() {
            return level;
        }

        public boolean le(Access other) {
            return level <= other.level();
        }
    }

    public InspectionImpl(Access access,
                          List<Comment> comments,
                          Source source,
                          boolean synthetic,
                          List<AnnotationExpression> annotations) {
        this.access = access;
        this.comments = comments;
        this.source = source;
        this.synthetic = synthetic;
        this.annotations = annotations;
    }

    @Override
    public Access access() {
        return access;
    }

    @Override
    public List<Comment> comments() {
        return comments;
    }

    @Override
    public Source source() {
        return source;
    }

    @Override
    public boolean isSynthetic() {
        return synthetic;
    }

    @Override
    public List<AnnotationExpression> annotations() {
        return annotations;
    }

    @SuppressWarnings("unchecked")
    public static abstract class Builder<B extends Info.Builder<?>> implements Inspection, Info.Builder<B> {
        private Access access;
        private List<Comment> comments;
        private Source source;
        private boolean synthetic;
        private List<AnnotationExpression> annotations;

        @Fluent
        public B setAccess(Access access) {
            this.access = access;
            return (B) this;
        }

        @Fluent
        public B setAnnotations(List<AnnotationExpression> annotations) {
            this.annotations = annotations;
            return (B) this;
        }

        @Fluent
        public B setComments(List<Comment> comments) {
            this.comments = comments;
            return (B) this;
        }

        @Fluent
        public B setSource(Source source) {
            this.source = source;
            return (B) this;
        }

        @Override
        public B addComment(Comment comment) {
            if (comments == null) comments = new ArrayList<>();
            comments.add(comment);
            return (B) this;
        }

        @Override
        public B addComments(List<Comment> comments) {
            if (this.comments == null) this.comments = new ArrayList<>();
            this.comments.addAll(comments);
            return (B) this;
        }

        @Override
        public B addAnnotation(AnnotationExpression annotation) {
            if (this.annotations == null) this.annotations = new ArrayList<>();
            annotations.add(annotation);
            return (B) this;
        }

        @Override
        public B addAnnotations(List<AnnotationExpression> annotations) {
            if (this.annotations == null) this.annotations = new ArrayList<>();
            this.annotations.addAll(annotations);
            return (B) this;
        }

        @Fluent
        public B setSynthetic(boolean synthetic) {
            this.synthetic = synthetic;
            return (B) this;
        }

        @Override
        public Access access() {
            return access;
        }

        @Override
        public List<Comment> comments() {
            return comments;
        }

        @Override
        public Source source() {
            return source;
        }

        @Override
        public boolean isSynthetic() {
            return synthetic;
        }

        @Override
        public List<AnnotationExpression> annotations() {
            return annotations;
        }
    }
}
