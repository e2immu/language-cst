package org.e2immu.language.cst.api.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;

import java.util.List;
import java.util.stream.Stream;

public interface AnnotationExpression extends Expression {

    interface KV {
        String key();

        /* in Java, that would be "value" */
        boolean keyIsDefault();

        KV rewire(InfoMap infoMap);

        KV translate(TranslationMap translationMap);

        Expression value();
    }

    TypeInfo typeInfo();

    List<KV> keyValuePairs();

    interface Builder extends Element.Builder<Builder> {
        @Fluent
        Builder addKeyValuePair(String key, Expression value);

        @Fluent
        Builder setKeyValuesPairs(List<KV> kvs);

        @Fluent
        Builder setTypeInfo(TypeInfo typeInfo);

        AnnotationExpression build();
    }

    int[] extractIntArray(String key);

    String[] extractStringArray(String key);

    boolean extractBoolean(String key);

    String extractString(String key, String defaultValue);

    AnnotationExpression withKeyValuePair(String key, Expression value);
}
