package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Block;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.type.TypeParameter;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.support.EventuallyFinal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

        @Override
        public boolean isCompactConstructor() {
            return this == COMPACT_CONSTRUCTOR;
        }

        @Override
        public boolean isAbstract() {
            return this == ABSTRACT_METHOD;
        }

        @Override
        public boolean isDefault() {
            return this == DEFAULT_METHOD;
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
        return analysis().getOrDefault(PropertyImpl.NOT_NULL_METHOD, ValueImpl.NotNullImpl.NULLABLE).isAtLeastNotNull();
    }

    @Override
    public boolean isPropertyNullable() {
        return analysis().getOrDefault(PropertyImpl.NOT_NULL_METHOD, ValueImpl.NotNullImpl.NULLABLE).isNullable();
    }

    public void commit(MethodInspection methodInspection) {
        try {
            inspection.setFinal(methodInspection);
        } catch (IllegalStateException ise) {
            throw new RuntimeException("Have already committed method '" + fullyQualifiedName() + "'");
        }
    }

    @Override
    public boolean isConstructor() {
        return methodType.isConstructor();
    }

    private static final Map<String, Integer> JLO_METHODS = Map.of("clone", 0, "equals", 1,
            "finalize", 0, "getClass", 0, "hashCode", 0, "notify", 0,
            "notifyAll", 0, "toString", 0, "wait", 0);

    @Override
    public boolean isOverloadOfJLOMethod() {
        int n = parameters().size();
        Integer i = JLO_METHODS.get(name);
        if (i != null) {
            if (i == n) return true;
            if ("wait".equals(name)) return i <= 2;
        }
        return false;
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
        if (methodBody == null || methodBody.isEmpty()) return isAbstract() ? 2 : 10;
        return methodBody.complexity();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Visitor visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new MethodPrinter(this).print(qualification);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        Block methodBody = inspection.get().methodBody();
        return methodBody == null ? Stream.empty() : methodBody.variables(descendMode);
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        Stream<TypeReference> fromReturnType = hasReturnValue() && returnType().bestTypeInfo() != null
                ? Stream.of(new ElementImpl.TypeReference(returnType().bestTypeInfo(), true)) : Stream.empty();
        Stream<TypeReference> fromParameters = parameters().stream()
                .flatMap(pi -> pi.parameterizedType().typesReferenced().map(TypeReference::withExplicit));
        Stream<TypeReference> fromAnnotations = annotations().stream().flatMap(AnnotationExpression::typesReferenced);
        Stream<TypeReference> fromExceptionTypes = exceptionTypes().stream()
                .flatMap(et -> et.typesReferenced().map(TypeReference::withExplicit));
        Stream<TypeReference> fromBody = methodBody().typesReferenced();
        return Stream.concat(fromReturnType, Stream.concat(fromParameters, Stream.concat(fromAnnotations,
                Stream.concat(fromExceptionTypes, fromBody))));
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
        return methodType.isDefault();
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
        return methodType.isStatic();
    }

    @Override
    public ParameterizedType returnType() {
        return inspection.get().returnType();
    }

    @Override
    public Set<MethodInfo> topOfOverloadingHierarchy() {
        Set<MethodInfo> overrides = overrides();
        if (overrides.isEmpty()) return Set.of(this);
        return overrides.stream()
                .flatMap(mi -> mi.topOfOverloadingHierarchy().stream())
                .collect(Collectors.toUnmodifiableSet());
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
        return methodType.isAbstract();
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
        return analysis().getOrDefault(PropertyImpl.COMMUTABLE_METHODS, ValueImpl.CommutableDataImpl.NONE);
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
        ValueImpl.FieldBooleanMapImpl value = analysis().getOrNull(PropertyImpl.OWN_FIELDS_READ_MODIFIED_IN_METHOD,
                ValueImpl.FieldBooleanMapImpl.class);
        return value == null ? null : value.map();
    }

    @Override
    public List<ParameterizedType> exceptionTypes() {
        return inspection.get().exceptionTypes();
    }

    @Override
    public boolean isFinal() {
        return inspection.get().modifiers().stream().anyMatch(MethodModifier::isFinal);
    }

    @Override
    public boolean hasBeenAnalyzed() {
        // TODO should add computational analyzer too, later
        return analysis().getOrDefault(PropertyImpl.SHALLOW_ANALYZER, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public MethodType methodType() {
        return methodType;
    }

    @Override
    public ParameterizedType typeOfParameterHandleVarargs(int index) {
        int formalParams = parameters().size();
        if (index < formalParams - 1 || index < formalParams && !isVarargs()) {
            return parameters().get(index).parameterizedType();
        }
        return parameters().get(formalParams - 1).parameterizedType().copyWithOneFewerArrays();
    }

    @Override
    public boolean noReturnValue() {
        return isVoid() || isConstructor();
    }

    @Override
    public boolean explicitlyEmptyMethod() {
        if (!methodBody().statements().isEmpty() || isStatic() && isSynthetic()) return false;
        boolean shallowAnalyzer = typeInfo.analysis()
                .getOrDefault(PropertyImpl.SHALLOW_ANALYZER, ValueImpl.BoolImpl.FALSE).isTrue();
        boolean empty = !shallowAnalyzer && !isAbstract();
        assert !empty || noReturnValue();
        return empty;
    }

    @Override
    public Set<MethodModifier> methodModifiers() {
        return inspection.get().modifiers();
    }

    @Override
    public boolean isFactoryMethod() {
        return isStatic() && returnType().typeInfo() != null
               && returnType().typeInfo().isEnclosedIn(typeInfo);
    }

    @Override
    public List<MethodInfo> translate(TranslationMap translationMap) {
        List<MethodInfo> direct = translationMap.translateMethod(this);
        if (direct.size() != 1 || direct.get(0) != this) {
            return direct;
        }
        ParameterizedType tReturnType = translationMap.translateType(returnType());
        boolean change = tReturnType != returnType();

        List<Statement> tBody = methodBody().translate(translationMap);
        change |= tBody.size() != 1 || tBody.get(0) != methodBody();

        List<ParameterInfo> newParameters = new ArrayList<>(2 * parameters().size());
        for (ParameterInfo pi : parameters()) {
            List<ParameterInfo> tPi = pi.translate(translationMap);
            newParameters.addAll(tPi);
            change |= tPi.size() != 1 || tPi.get(0) != pi;
        }
        List<ParameterizedType> exceptionTypeList = exceptionTypes();
        List<ParameterizedType> newExceptionTypes = exceptionTypeList
                .stream().map(translationMap::translateType).collect(translationMap.toList(exceptionTypeList));
        change |= newExceptionTypes != exceptionTypeList;

        if (change) {
            MethodInfo methodInfo = copyAllButBodyParametersReturnTypeAnnotationsExceptionTypes();
            MethodInfo.Builder builder = methodInfo.builder();
            builder.setMethodBody((Block) tBody.get(0));
            newParameters.forEach(builder::addParameter);
            newExceptionTypes.forEach(builder::addExceptionType);
            builder.setReturnType(tReturnType);
            builder.commit();
            return List.of(methodInfo);
        }
        return List.of(this);
    }

    private MethodInfo copyAllButBodyParametersReturnTypeAnnotationsExceptionTypes() {
        MethodInfo methodInfo = new MethodInfoImpl(methodType, name, typeInfo);
        MethodInfo.Builder builder = methodInfo.builder();
        builder.setAccess(access()).setSource(source()).setSynthetic(isSynthetic());
        methodInfo.typeParameters().forEach(builder::addTypeParameter);
        methodInfo.methodModifiers().forEach(builder::addMethodModifier);
        return methodInfo;
    }

    @Override
    public MethodInfo withMethodBody(Block newBody) {
        MethodInfo methodInfo = copyAllButBodyParametersReturnTypeAnnotationsExceptionTypes();
        MethodInfo.Builder builder = methodInfo.builder();
        exceptionTypes().forEach(builder::addExceptionType);
        annotations().forEach(builder::addAnnotation);
        builder.setReturnType(returnType());
        parameters().forEach(builder::addParameter);
        builder.commitParameters();
        builder.setMethodBody(newBody);
        builder.commit();
        return methodInfo;
    }
}
