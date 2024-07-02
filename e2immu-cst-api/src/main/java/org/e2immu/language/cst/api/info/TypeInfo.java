package org.e2immu.language.cst.api.info;


import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.element.CompilationUnit;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.type.NamedType;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.type.TypeNature;
import org.e2immu.language.cst.api.type.TypeParameter;
import org.e2immu.support.Either;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface TypeInfo extends NamedType, Info {

    String fullyQualifiedName();

    // for java, that will be packageName == "java.lang"
    boolean doesNotRequirePackage();

    default CompilationUnit compilationUnit() {
        return compilationUnitOrEnclosingType().isLeft() ? compilationUnitOrEnclosingType().getLeft()
                : compilationUnitOrEnclosingType().getRight().compilationUnit();
    }

    boolean hasBeenInspected();

    boolean isAnnotation();

    boolean isEnclosedIn(TypeInfo typeInfo);

    default boolean isPrimaryType() {
        return compilationUnitOrEnclosingType().isLeft();
    }

    boolean isSealedOrFinal();

    String packageName();

    // chain of type names Primary.Sub.Sub2
    String fromPrimaryTypeDownwards();

    Either<CompilationUnit, TypeInfo> compilationUnitOrEnclosingType();

    // from inspection
    Set<TypeInfo> superTypesExcludingJavaLangObject();

    List<TypeParameter> typeParameters();

    MethodInfo findUniqueMethod(String methodName, int n);

    MethodInfo findConstructor(int i);

    TypeInfo findSubType(String simpleName);

    TypeInfo findSubType(String simpleName, boolean complain);

    FieldInfo getFieldByName(String name, boolean complain);

    MethodInfo findUniqueMethod(String tryCatch, TypeInfo typeInfoOfFirstParameter);

    ParameterizedType parentClass();

    List<ParameterizedType> interfacesImplemented();

    default List<MethodInfo> methods() {
        return methodStream(null).toList();
    }

    boolean isPrimitiveExcludingVoid();

    boolean isJavaIoSerializable();

    boolean isJavaLangObject();

    /**
     * @return null when the type is not a functional interface
     */
    MethodInfo singleAbstractMethod();

    boolean isNumeric();

    boolean isBoxedExcludingVoid();

    boolean isFunctionalInterface();

    boolean isJavaLangString();

    boolean isJavaLangClass();

    boolean isVoid();

    boolean isJavaLangVoid();

    boolean isBoxedBoolean();

    List<TypeInfo> subTypes();

    boolean isPrivate();

    boolean isAbstract();

    boolean isCharacter();

    boolean isBoxedLong();

    boolean isInteger();

    boolean isBoxedShort();

    boolean isBoxedByte();

    boolean isBoxedDouble();

    boolean isBoxedFloat();

    interface Methods {

    }

    default Stream<MethodInfo> methodStream() {
        return methodStream(null);
    }

    Stream<MethodInfo> methodStream(Methods methods);

    List<MethodInfo> constructors();

    List<FieldInfo> fields();

    boolean isStatic();

    boolean isInterface();

    TypeInfo primaryType();

    boolean isBoolean();

    boolean isInt();

    boolean isLong();

    boolean isShort();

    boolean isByte();

    boolean isFloat();

    boolean isDouble();

    boolean isChar();

    boolean isPublic();

    void setOnDemandInspection(Consumer<TypeInfo> inspector);

    boolean haveOnDemandInspection();

    Builder builder();

    TypeNature typeNature();

    interface Builder extends Info.Builder<Builder> {
        @Fluent
        Builder addSubType(TypeInfo subType);

        @Fluent
        Builder addTypeModifier(TypeModifier typeModifier);

        @Fluent
        Builder addMethod(MethodInfo methodInfo);

        @Fluent
        Builder addField(FieldInfo field);

        @Fluent
        Builder addConstructor(MethodInfo constructor);

        @Fluent
        Builder setTypeNature(TypeNature typeNature);

        @Fluent
        Builder setParentClass(ParameterizedType parentClass);

        @Fluent
        Builder addInterfaceImplemented(ParameterizedType interfaceImplemented);

        @Fluent
        Builder addTypeParameter(TypeParameter typeParameter);

        @Fluent
        Builder setSingleAbstractMethod(MethodInfo singleAbstractMethod);
    }

    boolean isAtLeastImmutableHC();

    OutputBuilder print(Qualification qualification, boolean doTypeDeclaration);

    // as part of type resolution
    boolean fieldsAccessedInRestOfPrimaryType();


    default Stream<TypeInfo> recursiveSubTypeStream() {
        return Stream.concat(Stream.of(this), subTypes().stream().flatMap(TypeInfo::recursiveSubTypeStream));
    }

    default Stream<TypeInfo> recursiveSuperTypeStream() {
        Stream<TypeInfo> s1;
        if (compilationUnitOrEnclosingType().isRight() && !isStatic()) {
            TypeInfo right = compilationUnitOrEnclosingType().getRight();
            s1 = Stream.concat(Stream.of(right), right.recursiveSuperTypeStream());
        } else {
            s1 = Stream.of();
        }
        Stream<TypeInfo> s2;
        if (parentClass() != null) {
            TypeInfo parent = parentClass().bestTypeInfo();
            s2 = Stream.concat(Stream.of(parent), parent.recursiveSuperTypeStream());
        } else {
            s2 = Stream.of();
        }
        Stream<TypeInfo> s3 = interfacesImplemented().stream().map(ParameterizedType::bestTypeInfo)
                .filter(Objects::nonNull)
                .flatMap(ti -> Stream.concat(Stream.of(ti), ti.recursiveSuperTypeStream()));
        return Stream.concat(s1, Stream.concat(s2, s3));
    }
}
