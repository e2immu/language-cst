package org.e2immu.language.cst.api.info;

import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.element.CompilationUnit;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.statement.Block;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.type.TypeParameter;
import org.e2immu.language.cst.api.util.ParSeq;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MethodInfo extends Info {

    List<ParameterizedType> exceptionTypes();

    boolean isConstructor();

    boolean isFinal();

    boolean isOverloadOfJLOMethod();

    boolean isSynchronized();

    TypeInfo primaryType();

    boolean isDefault();

    boolean isVoid();

    boolean complexityGreaterThanCOMPLEXITY_METHOD_WITHOUT_CODE();

    default boolean hasReturnValue() {
        return !isVoid() && !isConstructor();
    }

    boolean isPostfix();

    boolean isInfix();

    boolean isSynthetic();

    boolean isAbstract();

    interface MethodType {
        boolean isStatic();

        boolean isConstructor();
    }

    String name();

    String fullyQualifiedName();

    TypeInfo typeInfo();

    boolean isStatic();

    ParameterizedType returnType();

    Set<MethodInfo> topOfOverloadingHierarchy();

    // from inspection

    List<ParameterInfo> parameters();

    List<TypeParameter> typeParameters();

    Block methodBody();

    // from resolution

    boolean isOverloadOf(MethodInfo methodInfo);

    Set<MethodInfo> overrides();

    boolean allowsInterrupts();

    // with inspection

    boolean isPublic();

    boolean isPubliclyAccessible();

    boolean isOverloadOfJLOEquals();

    boolean isCompactConstructor();

    boolean isSyntheticConstructor();

    boolean isStaticBlock();

    // from analysis

    boolean isModifying();

    boolean isFluent();

    boolean isIdentity();

    boolean isStaticSideEffects();

    Map<FieldInfo, Boolean> areOwnFieldsReadModified();

    // related to the commutation of methods

    Value.CommutableData commutableData();

    // related to the commutation of parameters

    Value.ParameterParSeq getParallelGroups();

    default boolean hasParallelGroups() {
        ParSeq<ParameterInfo> parSeq = getParallelGroups().parSeq();
        return parSeq != null && parSeq.containsParallels();
    }

    default List<Expression> sortAccordingToParallelGroupsAndNaturalOrder(List<Expression> parameterExpressions) {
        return getParallelGroups().parSeq().sortParallels(parameterExpressions, Comparator.naturalOrder());
    }

    // this method acts as a getter or setter for this field
    Value.FieldValue getSetField();

    // there is another method without these parameters; they can also be set with setters
    Value.GetSetEquivalent getSetEquivalents();

    Value.PostConditions postConditions();

    Value.Precondition precondition();

    /*
       Many throw and assert statements find their way into a pre- or post-condition.
       Some, however, do not. We register them here.
     */
    Value.IndicesOfEscapes indicesOfEscapesNotInPreOrPostConditions();

    Builder builder();

    boolean isPropertyNotNull();

    boolean isPropertyNullable();

    default CompilationUnit compilationUnit() {
        return typeInfo().compilationUnit();
    }

    interface Builder extends Info.Builder<Builder> {
        /**
         * Intermediate step: the fully qualified name can now be computed, because all
         * parameters are known.
         *
         * @return the builder
         */
        @Fluent
        Builder commitParameters();

        @Fluent
        Builder setMethodBody(Block block);

        @Fluent
        Builder addMethodModifier(MethodModifier methodModifier);

        /**
         * This method directly commits the builder, without any changes.
         */
        @Fluent
        Builder addAndCommitParameter(String name, ParameterizedType type);

        @Fluent
        Builder setReturnType(ParameterizedType returnType);

        ParameterInfo addParameter(String name, ParameterizedType type);

        @Fluent
        Builder addTypeParameter(TypeParameter typeParameter);

        @Fluent
        Builder addExceptionType(ParameterizedType exceptionType);
    }

    default boolean isVarargs() {
        if (parameters().isEmpty()) return false;
        return parameters().get(parameters().size() - 1).isVarArgs();
    }
}

