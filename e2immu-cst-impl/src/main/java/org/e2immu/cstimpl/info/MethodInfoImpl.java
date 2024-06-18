package org.e2immu.cstimpl.info;

import org.e2immu.cstapi.analysis.Value;
import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.info.*;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.Block;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.type.TypeParameter;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.analysis.PropertyImpl;
import org.e2immu.cstimpl.analysis.ValueImpl;
import org.e2immu.support.EventuallyFinal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MethodInfoImpl extends InfoImpl implements MethodInfo {

    public enum MethodTypeEnum implements MethodType {
        CONSTRUCTOR(true), COMPACT_CONSTRUCTOR(true), SYNTHETIC_CONSTRUCTOR(true),
        STATIC_BLOCK(false), DEFAULT_METHOD(false), STATIC_METHOD(false),
        ABSTRACT_METHOD(false), METHOD(false);
        final boolean constructor;

        MethodTypeEnum(boolean constructor) {
            this.constructor = constructor;
        }

        @Override
        public boolean isConstructor() {
            return constructor;
        }

        @Override
        public boolean isStatic() {
            return this == STATIC_BLOCK || this == STATIC_METHOD;
        }
    }

    private final TypeInfo typeInfo; // back reference, only @ContextClass after...
    private final String name;
    private final MethodInfo.MethodType methodType;
    private final EventuallyFinal<MethodInspection> inspection = new EventuallyFinal<>();

    public MethodInfoImpl(TypeInfo typeInfo) {
        this(MethodTypeEnum.CONSTRUCTOR, "<init>", typeInfo);
    }

    public MethodInfoImpl(TypeInfo typeInfo, MethodType methodType) {
        this(methodType, "<init>", typeInfo);
    }

    public MethodInfoImpl(MethodInfo.MethodType methodType,
                          String name,
                          TypeInfo typeInfo) {
        this.name = name;
        this.methodType = methodType;
        this.typeInfo = typeInfo;
        inspection.setVariable(new MethodInspectionImpl.Builder(this));
    }

    public MethodInspectionImpl.Builder inspectionBuilder() {
        if (inspection.isVariable()) return (MethodInspectionImpl.Builder) inspection.get();
        throw new UnsupportedOperationException();
    }

    public boolean hasBeenCommitted() {
        return inspection.isFinal();
    }

    @Override
    public String toString() {
        return fullyQualifiedName();
    }

    @Override
    public MethodInfo.Builder builder() {
        if (inspection.isVariable()) return (MethodInfo.Builder) inspection.get();
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPropertyNotNull() {
        if (returnType().isPrimitiveExcludingVoid()) return true;
        return analysis().getOrDefault(PropertyImpl.NOT_NULL_METHOD, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public boolean isPropertyNullable() {
        return analysis().getOrDefault(PropertyImpl.NOT_NULL_METHOD, ValueImpl.BoolImpl.FALSE).isFalse();
    }

    public void commit(MethodInspection methodInspection) {
        inspection.setFinal(methodInspection);
    }

    @Override
    public boolean isConstructor() {
        return methodType.isConstructor();
    }

    @Override
    public boolean isOverloadOfJLOMethod() {
        if ("equals".equals(name) && parameters().size() == 1) return true;
        if ("hashCode".equals(name) && parameters().isEmpty()) return true;
        return "toString".equals(name()) && parameters().isEmpty();
    }

    @Override
    public TypeInfo primaryType() {
        return typeInfo.primaryType();
    }

    @Override
    public boolean isVoid() {
        return inspection.get().returnType().isVoid();
    }

    @Override
    public int complexity() {
        Block methodBody = inspection.get().methodBody();
        return methodBody == null ? 10 : methodBody.complexity();
    }

    @Override
    public List<Comment> comments() {
        return inspection.get().comments();
    }

    @Override
    public Source source() {
        return inspection.get().source();
    }

    @Override
    public void visit(Predicate<Element> predicate) {

    }

    @Override
    public void visit(Visitor visitor) {

    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return null;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        Block methodBody = inspection.get().methodBody();
        return methodBody == null ? Stream.empty() : methodBody.variables(descendMode);
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        throw new UnsupportedOperationException(); // FIXME
    }

    @Override
    public boolean complexityGreaterThanCOMPLEXITY_METHOD_WITHOUT_CODE() {
        return false;
    }


    @Override
    public boolean isPostfix() {
        return inspection.get().operatorType() == MethodInspection.OperatorType.POSTFIX;
    }

    @Override
    public boolean isInfix() {
        return inspection.get().operatorType() == MethodInspection.OperatorType.INFIX;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String fullyQualifiedName() {
        return inspection.get().fullyQualifiedName();
    }

    @Override
    public TypeInfo typeInfo() {
        return typeInfo;
    }

    @Override
    public boolean isDefault() {
        return methodType == MethodTypeEnum.DEFAULT_METHOD;
    }

    public boolean isCompactConstructor() {
        return methodType == MethodTypeEnum.COMPACT_CONSTRUCTOR;
    }

    public boolean isSyntheticConstructor() {
        return methodType == MethodTypeEnum.SYNTHETIC_CONSTRUCTOR;
    }

    public boolean isStaticBlock() {
        return methodType == MethodTypeEnum.STATIC_BLOCK;
    }

    @Override
    public boolean isStatic() {
        return methodType == MethodTypeEnum.STATIC_METHOD || methodType == MethodTypeEnum.STATIC_BLOCK;
    }

    @Override
    public ParameterizedType returnType() {
        return inspection.get().returnType();
    }

    @Override
    public Set<MethodInfo> topOfOverloadingHierarchy() {
        return Set.of();
    }

    @Override
    public List<ParameterInfo> parameters() {
        return inspection.get().parameters();
    }

    @Override
    public boolean isOverloadOf(MethodInfo methodInfo) {
        return false;
    }

    @Override
    public boolean isOverloadOfJLOEquals() {
        return parameters().size() == 1 && "equals".equals(name);
    }

    @Override
    public Set<MethodInfo> overrides() {
        return inspection.get().overrides();
    }

    @Override
    public boolean allowsInterrupts() {
        return analysis().getOrDefault(PropertyImpl.METHOD_ALLOWS_INTERRUPTS, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public boolean isPublic() {
        return inspection.get().isPublic();
    }

    @Override
    public boolean isPubliclyAccessible() {
        if (!isPublic()) return false;
        return typeInfo.isPublic();
    }

    @Override
    public boolean isSynthetic() {
        return inspection.get().isSynthetic();
    }

    @Override
    public boolean isAbstract() {
        return inspection.get().modifiers().stream().anyMatch(MethodModifier::isAbstract);
    }

    @Override
    public boolean isModifying() {
        return analysis().getOrDefault(PropertyImpl.MODIFIED_METHOD, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public boolean isFluent() {
        return analysis().getOrDefault(PropertyImpl.FLUENT_METHOD, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public boolean isIdentity() {
        return analysis().getOrDefault(PropertyImpl.IDENTITY_METHOD, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public Value.CommutableData commutableData() {
        return analysis().getOrDefault(PropertyImpl.COMMUTABLE_METHODS, ValueImpl.CommutableDataImpl.BLANK);
    }

    @Override
    public Value.ParameterParSeq getParallelGroups() {
        return analysis().getOrDefault(PropertyImpl.PARALLEL_PARAMETER_GROUPS, ValueImpl.ParameterParSeqImpl.EMPTY);
    }

    @Override
    public Value.FieldValue getSetField() {
        return analysis().getOrDefault(PropertyImpl.GET_SET_FIELD, ValueImpl.FieldValueImpl.EMPTY);
    }

    @Override
    public Value.GetSetEquivalent getSetEquivalents() {
        return analysis().getOrDefault(PropertyImpl.GET_SET_EQUIVALENT, ValueImpl.GetSetEquivalentImpl.EMPTY);
    }

    @Override
    public Value.PostConditions postConditions() {
        return analysis().getOrDefault(PropertyImpl.POST_CONDITIONS_METHOD, ValueImpl.PostConditionsImpl.EMPTY);
    }

    @Override
    public Value.Precondition precondition() {
        return analysis().getOrDefault(PropertyImpl.PRECONDITION_METHOD, ValueImpl.PreconditionImpl.EMPTY);
    }

    @Override
    public List<AnnotationExpression> annotations() {
        return inspection.get().annotations();
    }

    @Override
    public Block methodBody() {
        return inspection.get().methodBody();
    }

    @Override
    public Access access() {
        return inspection.get().access();
    }

    @Override
    public List<TypeParameter> typeParameters() {
        return inspection.get().typeParameters();
    }

    @Override
    public boolean isStaticSideEffects() {
        return analysis().getOrDefault(PropertyImpl.STATIC_SIDE_EFFECTS_METHOD, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public Value.IndicesOfEscapes indicesOfEscapesNotInPreOrPostConditions() {
        return analysis().getOrDefault(PropertyImpl.INDICES_OF_ESCAPE_METHOD, ValueImpl.IndicesOfEscapesImpl.EMPTY);
    }

    @Override
    public boolean isSynchronized() {
        return inspection.get().modifiers().stream().anyMatch(MethodModifier::isSynchronized);
    }

    @Override
    public Map<FieldInfo, Boolean> areOwnFieldsReadModified() {
        return analysis().getOrDefault(PropertyImpl.OWN_FIELDS_READ_MODIFIED_IN_METHOD, ValueImpl.FieldBooleanMapImpl.EMPTY).map();
    }
}
