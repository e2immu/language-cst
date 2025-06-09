package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.*;
import org.e2immu.language.cst.api.info.Info;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.GuideImpl;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SpaceEnum;
import org.e2immu.language.cst.impl.output.TextImpl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Convention: the "comment" string in {@link MultiLineComment} has placeholders {\\d+}, one for each tag in this object.
 */
public class JavaDocImpl extends MultiLineComment implements JavaDoc {

    public static class TagImpl implements Tag {
        private final TagIdentifier tagIdentifier;
        private final String content;
        private final Element resolvedReference;
        private final Source source;
        private final boolean blockTag;

        public TagImpl(TagIdentifier tagIdentifier, String content, Element resolvedReference, Source source, boolean blockTag) {
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
        public Element resolvedReference() {
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
            if (resolvedReference instanceof Info info) {
                List<? extends Info> infos = info.translate(translationMap);
                if (infos.size() != 1 || infos.getFirst() != info) {
                    return new TagImpl(tagIdentifier, content, infos.getFirst(), source, blockTag);
                }
            }
            return this;
        }

        @Override
        public String toString() {
            if (blockTag) {
                return "@" + tagIdentifier.identifier + (content.isEmpty() ? "" : " " + content);
            }
            return "{@" + tagIdentifier.identifier + " " + content + "}";
        }

        @Override
        public Tag withResolvedReference(Element resolvedReference) {
            return new TagImpl(tagIdentifier, content, resolvedReference, source, blockTag);
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
        return new JavaDocImpl(source(), super.comment(), tags.stream().map(t -> t.rewire(infoMap)).toList());
    }

    @Override
    public JavaDoc translate(TranslationMap translationMap) {
        List<Tag> translatedTags = tags.stream().map(tag -> tag.translate(translationMap))
                .collect(TranslationMap.staticToList(tags));
        if (translatedTags == tags) {
            return this;
        }
        return new JavaDocImpl(source(), super.comment(), translatedTags);
    }

    @Override
    public JavaDoc withTags(List<Tag> newTags) {
        return new JavaDocImpl(source(), super.comment(), newTags);
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

    @Override
    public String toString() {
        return "javaDoc@" + source().compact2();
    }

    // IMPROVE spacing! See TestJavaDoc,1
    @Override
    public OutputBuilder print(Qualification qualification) {
        GuideImpl.GuideGenerator gg = GuideImpl.generatorForMultilineComment();
        String text = "/*" + comment() + "*/";
        OutputBuilder joinedText = Arrays.stream(text.split("\n"))
                .filter(line -> !line.isBlank())
                .map(line -> new OutputBuilderImpl().add(new TextImpl(line.trim())))
                .collect(OutputBuilderImpl.joining(SpaceEnum.NEWLINE, gg));
        return new OutputBuilderImpl().add(joinedText);
    }
}
