package org.e2immu.language.cst.api.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;

import java.util.List;

public interface AnnotationExpression {

    OutputBuilder print(Qualification qualification);

    interface KV {

        String key();
        /* in Java, that would be "value" */

        boolean keyIsDefault();
        Expression value();

    }
    TypeInfo typeInfo();

    List<KV> keyValuePairs();

    interface Builder {

        @Fluent
        Builder addKeyValuePair(String key, Expression value);
        @Fluent
        Builder setTypeInfo(TypeInfo typeInfo);

        AnnotationExpression build();

    }
    boolean extractBoolean(String key);

    String extractString(String key);

}
