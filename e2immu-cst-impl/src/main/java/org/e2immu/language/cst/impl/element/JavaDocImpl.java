package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.*;
import org.e2immu.language.cst.api.info.Info;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/*
Convention: the "comment" string in MultiLineComment has placeholders {\\d+}, one for each tag in this object.
 */
public class JavaDocImpl extends MultiLineComment implements JavaDoc {

    public static class TagImpl implements Tag {
        private final TagIdentifier tagIdentifier;
        private final String content;
        private final Info resolvedReference;
        private final Source source;
        private final boolean blockTag;

        public TagImpl(TagIdentifier tagIdentifier, String content, Info resolvedReference, Source source, boolean blockTag) {
            this.tagIdentifier = tagIdentifier;
            this.content = content;
            this.resolvedReference = resolvedReference;
            this.source = source;
            this.blockTag = blockTag;
        }

        @Override
        public TagIdentifier identifier() {
            return tagIdentifier;
        }

        @Override
        public boolean blockTag() {
            return blockTag;
        }

        @Override
        public Source source() {
            return source;
        }

        @Override
        public Info resolvedReference() {
            return resolvedReference;
        }

        @Override
        public String content() {
            return content;
        }

        @Override
        public Tag rewire(InfoMap infoMap) {
            if (resolvedReference == null) return this;
            return new TagImpl(tagIdentifier, content, (Info) resolvedReference.rewire(infoMap), source, blockTag);
        }

        @Override
        public Tag translate(TranslationMap translationMap) {
            if (resolvedReference == null) return this;
            List<? extends Info> infos = resolvedReference.translate(translationMap);
            if (infos.size() != 1 || infos.getFirst() != resolvedReference) {
                return new TagImpl(tagIdentifier, content, infos.getFirst(), source, blockTag);
            }
            return this;
        }
    }

    private final List<Tag> tags;

    public JavaDocImpl(Source source, String comment, List<Tag> tags) {
        super(source, comment);
        this.tags = tags;
    }

    @Override
    public List<Tag> tags() {
        return tags;
    }

    @Override
    public String comment() {
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile("\\{(\\d+)}");
        Matcher m = p.matcher(super.comment());
        while (m.find()) {
            int tagIndex = Integer.parseInt(m.group(1));
            Tag tag = tags.get(tagIndex);
            m.appendReplacement(sb, tag.toString());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public int complexity() {
        return tags.size();
    }

    @Override
    public List<Comment> comments() {
        return List.of();
    }

    @Override
    public Element rewire(InfoMap infoMap) {
        return new JavaDocImpl(source(), comment(), tags.stream().map(t -> t.rewire(infoMap)).toList());
    }

    @Override
    public JavaDoc translate(TranslationMap translationMap) {
        List<Tag> translatedTags = tags.stream().map(tag -> tag.translate(translationMap))
                .collect(TranslationMap.staticToList(tags));
        if (translatedTags == tags) {
            return this;
        }
        return new JavaDocImpl(source(), comment(), translatedTags);
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        predicate.test(this);
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.beforeJavaDoc(this);
        visitor.afterJavaDoc(this);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.empty();
    }

    @Override
    public Stream<Variable> variableStreamDoNotDescend() {
        return Stream.empty();
    }

    @Override
    public Stream<Variable> variableStreamDescend() {
        return Stream.empty();
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        return Stream.empty();
    }
}
