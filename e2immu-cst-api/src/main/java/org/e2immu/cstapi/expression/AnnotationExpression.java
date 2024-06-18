package org.e2immu.cstapi.expression;

import org.e2immu.annotation.Fluent;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;

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
}
