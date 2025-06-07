package org.e2immu.language.cst.api.element;

import org.e2immu.language.cst.api.info.Info;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.language.cst.api.translate.TranslationMap;

import java.util.List;

public interface JavaDoc extends Element {

    enum TagIdentifier {
        AUTHOR("author"),
        CODE("code"),
        DEPRECATED("deprecated"),
        DOC_ROOT("docRoot"),
        EXCEPTION("exception"),
        HIDDEN("hidden"),
        INDEX("index"),
        INHERIT_DOC("inheritDoc"),
        LINK("link"),
        LINK_PLAIN("linkPlain"),
        LITERAL("literal"),
        PARAM("param"),
        PROVIDES("provides"),
        RETURN("return"),
        SEE("see"),
        SERIAL("serial"),
        SERIAL_DATA("serialData"),
        SERIAL_FIELD("serialField"),
        SINCE("since"),
        SNIPPET("snippet"),
        SPEC("spec"),
        SUMMARY("summary"),
        SYSTEM_PROPERTY("systemProperty"),
        THROWS("throws"),
        USES("uses"),
        VALUE("value"),
        VERSION("version");

        public final String identifier;

        TagIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }

    interface Tag {
        TagIdentifier identifier();

        boolean blockTag();

        Source source();

        Info resolvedReference();

        String content();

        Tag rewire(InfoMap infoMap);

        Tag translate(TranslationMap translationMap);
    }

    List<Tag> tags();

    JavaDoc translate(TranslationMap translationMap);
}
