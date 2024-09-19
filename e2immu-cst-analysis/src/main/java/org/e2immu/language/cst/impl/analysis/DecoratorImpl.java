package org.e2immu.language.cst.impl.analysis;

import org.e2immu.annotation.Modified;
import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.element.ImportStatement;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.runtime.Runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DecoratorImpl implements Qualification.Decorator {

    private final AnnotationExpression modifiedAnnotation;
    private final ImportStatement modifiedImport;

    private boolean modified;

    public DecoratorImpl(Runtime runtime) {
        TypeInfo modifiedTi = runtime.getFullyQualified(Modified.class, true);
        modifiedAnnotation = runtime.newAnnotationExpressionBuilder()
                .setTypeInfo(modifiedTi)
                .build();
        modifiedImport = runtime.newImportStatement(modifiedTi.fullyQualifiedName(), false);
    }

    @Override
    public List<AnnotationExpression> annotations(Info info) {
        boolean modified;
        if (info instanceof MethodInfo) {
            modified = info.analysis().getOrDefault(PropertyImpl.MODIFIED_METHOD, ValueImpl.BoolImpl.FALSE).isTrue();
        } else if (info instanceof FieldInfo) {
            modified = info.analysis().getOrDefault(PropertyImpl.MODIFIED_FIELD, ValueImpl.BoolImpl.FALSE).isTrue();
        } else if (info instanceof ParameterInfo) {
            modified = info.analysis().getOrDefault(PropertyImpl.MODIFIED_PARAMETER, ValueImpl.BoolImpl.FALSE).isTrue();
        } else {
            modified = false;
        }
        if (modified) {
            this.modified = true;
            return List.of(modifiedAnnotation);
        }
        return List.of();
    }

    @Override
    public List<ImportStatement> importStatements() {
        if (modified) {
            return List.of(modifiedImport);
        }
        return List.of();
    }
}
