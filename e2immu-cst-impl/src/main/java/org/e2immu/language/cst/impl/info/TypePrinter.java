package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.expression.ConstructorCall;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.type.TypeNature;
import org.e2immu.language.cst.api.type.TypeParameter;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.variable.ThisImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record TypePrinter(TypeInfo typeInfo) {

    public OutputBuilder output(Qualification qualification, boolean doTypeDeclaration) {
        TypeNature typeNature;
        Set<String> imports;
        Qualification insideType;
        if (typeInfo.isPrimaryType() && typeInfo.hasBeenInspected()) {
            ResultOfImportComputation res = imports(typeInfo.packageName(), typeInfo);
            imports = res.imports;
            insideType = res.qualification;
        } else {
            imports = Set.of();
            insideType = typeInfo.hasBeenInspected() && qualification instanceof QualificationImpl ? new QualificationImpl(false, qualification, null) : qualification;
        }
        assert insideType != null;

        List<TypeModifier> typeModifiers;
        List<FieldInfo> fields;
        List<MethodInfo> constructors;
        List<MethodInfo> methods;
        List<TypeInfo> subTypes;
        List<ParameterizedType> interfaces;
        List<TypeParameter> typeParameters;
        ParameterizedType parentClass;
        boolean isInterface;
        boolean isRecord;
       List< Comment> comments;
/*
        if (typeInfo.hasBeenInspected()) {
            TypeInspection typeInspection = typeInfo.typeInspection.get();
            typeNature = typeInspection.typeNature();
            isInterface = typeInspection.isInterface();
            isRecord = typeInspection.typeNature() == TypeNature.RECORD;
            typeModifiers = minimalModifiers(typeInspection);
            fields = typeInspection.fields();
            constructors = typeInspection.constructors();
            methods = typeInspection.methods();
            subTypes = typeInspection.subTypes();
            typeParameters = typeInspection.typeParameters();
            parentClass = typeInfo.parentIsNotJavaLangObject() ? typeInspection.parentClass() : null;
            interfaces = typeInspection.interfacesImplemented();
            comments = typeInspection.comments();

            // add the methods that we can call without having to qualify (method() instead of super.method())
            if (insideType instanceof QualificationImpl qi) {
                addMethodsToQualification(typeInfo, qi);
                addThisToQualification(typeInfo, qi);
            }
        } else {
            typeNature = TypeNatureEnum.CLASS; // we really have no idea what it is
            typeModifiers = List.of(TypeModifierEnum.ABSTRACT);
            fields = List.of();
            constructors = List.of();
            methods = List.of();
            subTypes = List.of();
            typeParameters = List.of();
            interfaces = List.of();
            parentClass = null;
            isInterface = false;
            isRecord = false;
            comments = null;
        }

        // PACKAGE AND IMPORTS

        OutputBuilder packageAndImports = new OutputBuilderImpl();
        if (typeInfo.isPrimaryType()) {
            String packageName = typeInfo.packageNameOrEnclosingType.getLeftOrElse("");
            if (!packageName.isEmpty()) {
                packageAndImports.add(Keyword.PACKAGE).add(Space.ONE).add(new Text(packageName)).add(Symbol.SEMICOLON)
                        .add(Space.NEWLINE);
            }
            if (!imports.isEmpty()) {
                imports.stream().sorted().forEach(i ->
                        packageAndImports.add(Keyword.IMPORT).add(Space.ONE).add(new Text(i)).add(Symbol.SEMICOLON)
                                .add(Space.NEWLINE));
            }
        }

        OutputBuilder afterAnnotations = new OutputBuilderImpl();
        if (doTypeDeclaration) {
            // the class name
            afterAnnotations
                    .add(typeModifiers.stream().map(mod -> new OutputBuilderImpl().add(mod.keyword))
                            .collect(OutputBuilder.joining(Space.ONE)))
                    .add(Space.ONE).add(typeNature.keyword)
                    .add(Space.ONE).add(new Text(typeInfo.simpleName));

            if (!typeParameters.isEmpty()) {
                afterAnnotations.add(Symbol.LEFT_ANGLE_BRACKET);
                afterAnnotations.add(typeParameters.stream().map(tp ->
                                tp.output(InspectionProvider.DEFAULT, insideType, new HashSet<>()))
                        .collect(OutputBuilder.joining(Symbol.COMMA)));
                afterAnnotations.add(Symbol.RIGHT_ANGLE_BRACKET);
            }
            if (isRecord) {
                afterAnnotations.add(outputNonStaticFieldsAsParameters(insideType, fields));
            }
            if (parentClass != null) {
                afterAnnotations.add(Space.ONE).add(Keyword.EXTENDS).add(Space.ONE).add(parentClass.output(insideType));
            }
            if (!interfaces.isEmpty()) {
                afterAnnotations.add(SpaceEnum.ONE).add(isInterface ? KeywordImpl.EXTENDS : KeywordImpl.IMPLEMENTS).add(SpaceEnum.ONE);
                afterAnnotations.add(interfaces.stream().map(pi -> pi.print(insideType)).collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)));
            }
        }

        OutputBuilder main = Stream.concat(Stream.concat(Stream.concat(Stream.concat(
                                                enumConstantStream(typeInfo, insideType),
                                                fields.stream()
                                                        .filter(f -> !f.fieldInspection.get().isSynthetic())
                                                        .map(f -> f.output(insideType, false))),
                                        subTypes.stream()
                                                .filter(st -> !st.typeInspection.get().isSynthetic())
                                                .map(ti -> ti.output(insideType, true))),
                                constructors.stream()
                                        .filter(c -> !c.methodInspection.get().isSynthetic())
                                        .map(c -> c.output(insideType))),
                        methods.stream()
                                .filter(m -> !m.methodInspection.get().isSynthetic())
                                .map(m -> m.output(insideType)))
                .collect(OutputBuilder.joining(Space.NONE, Symbol.LEFT_BRACE, Symbol.RIGHT_BRACE,
                        Guide.generatorForBlock()));
        afterAnnotations.add(main);

        // annotations and the rest of the type are at the same level
        Stream<OutputBuilder> annotationStream = doTypeDeclaration ? typeInfo.buildAnnotationOutput(insideType) : Stream.of();
        if (comments != null) packageAndImports.add(comments.output(qualification));
        return packageAndImports.add(Stream.concat(annotationStream, Stream.of(afterAnnotations))
                .collect(OutputBuilder.joining(Space.ONE_REQUIRED_EASY_SPLIT,
                        Guide.generatorForAnnotationList())));*/ return null;
    }

    private static List<TypeModifier> minimalModifiers(TypeInfo typeInspection) {
        Set<TypeModifier> modifiers = typeInspection.typeModifiers();
        List<TypeModifier> list = new ArrayList<>();

        // access
        Access access = typeInspection.access();
        Access enclosedAccess = typeInspection.compilationUnitOrEnclosingType().isLeft()
                ? InspectionImpl.AccessEnum.PUBLIC
                : typeInspection.compilationUnitOrEnclosingType().getRight().access();
        if (!enclosedAccess.isPrivate() && !access.isPackage()) {
            list.add(typeModifier(access));
        } // else there really is no point anymore to show any access modifier, let's keep it brief

        // 'abstract', 'static'
        if (typeInspection.typeNature().isClass()) {
            if (modifiers.contains(TypeModifierEnum.ABSTRACT)) {
                list.add(TypeModifierEnum.ABSTRACT);
            }
            if (modifiers.contains(TypeModifierEnum.STATIC)) {
                list.add(TypeModifierEnum.STATIC);
            }
            if (modifiers.contains(TypeModifierEnum.FINAL)) {
                list.add(TypeModifierEnum.FINAL);
            }
            if (modifiers.contains(TypeModifierEnum.SEALED)) {
                list.add(TypeModifierEnum.SEALED);
            }
            if (modifiers.contains(TypeModifierEnum.NON_SEALED)) {
                list.add(TypeModifierEnum.NON_SEALED);
            }
        } // else: records, interfaces, annotations, primitives are always static, never abstract

        return list;
    }

    private static TypeModifier typeModifier(Access access) {
        if(access.isPublic()) return TypeModifierEnum.PUBLIC;
        if(access.isProtected()) return TypeModifierEnum.PROTECTED;
        if(access.isPrivate()) return TypeModifierEnum.PRIVATE;
        throw new UnsupportedOperationException();
    }

    private static OutputBuilder outputNonStaticFieldsAsParameters(Qualification qualification, List<FieldInfo> fields) {
        return fields.stream()
                .filter(fieldInfo -> !fieldInfo.isStatic())
                .map(fieldInfo -> fieldInfo.print(qualification))
                .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA, SymbolEnum.LEFT_PARENTHESIS, SymbolEnum.RIGHT_PARENTHESIS,
                        GuideImpl.generatorForParameterDeclaration()));
    }

    private static void addThisToQualification(TypeInfo typeInfo, QualificationImpl insideType) {
        insideType.addThis(new ThisImpl(typeInfo));
        ParameterizedType parentClass = typeInfo.parentClass();
        if (parentClass != null && !parentClass.isJavaLangObject()) {
            insideType.addThis(new ThisImpl(parentClass.typeInfo(), null, true));
        }
    }

    private static void addMethodsToQualification(TypeInfo typeInfo, QualificationImpl qImpl) {
        typeInfo.methods().forEach(qImpl::addMethodUnlessOverride);
        if (!typeInfo.isJavaLangObject()) {
            addMethodsToQualification(typeInfo.parentClass().typeInfo(), qImpl);
        }
        for (ParameterizedType interfaceType : typeInfo.interfacesImplemented()) {
            addMethodsToQualification(interfaceType.typeInfo(), qImpl);
        }
    }

    private static Stream<OutputBuilder> enumConstantStream(TypeInfo typeInfo, Qualification qualification) {
        if (typeInfo.typeNature().isEnum()) {
            GuideImpl.GuideGenerator gg = GuideImpl.generatorForEnumDefinitions();
            OutputBuilder outputBuilder = new OutputBuilderImpl().add(gg.start());
            boolean first = true;
            for (FieldInfo fieldInfo : typeInfo.fields()) {
                if (fieldInfo.isSynthetic()) {
                    if (first) {
                        first = false;
                    } else {
                        outputBuilder.add(SymbolEnum.COMMA).add(gg.mid());
                    }
                    outputBuilder.add(new TextImpl(fieldInfo.name()));
                    Expression initializer = fieldInfo.initializer();
                    ConstructorCall constructorCall;
                    if (initializer != null && (constructorCall = initializer.asInstanceOf(ConstructorCall.class)) != null) {
                        if (!constructorCall.parameterExpressions().isEmpty()) {
                            GuideImpl.GuideGenerator args = GuideImpl.defaultGuideGenerator();
                            outputBuilder.add(SymbolEnum.LEFT_PARENTHESIS).add(args.start());
                            boolean firstParam = true;
                            for (Expression expression : constructorCall.parameterExpressions()) {
                                if (firstParam) {
                                    firstParam = false;
                                } else {
                                    outputBuilder.add(SymbolEnum.COMMA).add(args.mid());
                                }
                                outputBuilder.add(expression.print(qualification));
                            }
                            outputBuilder.add(args.end()).add(SymbolEnum.RIGHT_PARENTHESIS);
                        }
                    } else if (initializer != null) {
                        throw new UnsupportedOperationException("Expect initialiser to be a NewObject");
                    }
                }
            }
            outputBuilder.add(gg.end()).add(SymbolEnum.SEMICOLON);
            return Stream.of(outputBuilder);
        }
        return Stream.of();
    }


    record ResultOfImportComputation(Set<String> imports, QualificationImpl qualification) {
    }

    private static class PerPackage {
        final List<TypeInfo> types = new LinkedList<>();
        boolean allowStar = true;
    }

    private static ResultOfImportComputation imports(String myPackage, TypeInfo typeInfo) {
        Set<TypeInfo> typesReferenced = typeInfo.typesReferenced()
                .filter(Element.TypeReference::explicit)
                .map(Element.TypeReference::typeInfo)
                .filter(TypePrinter::allowInImport)
                .collect(Collectors.toSet());
        Map<String, PerPackage> typesPerPackage = new HashMap<>();
        QualificationImpl qualification =  new QualificationImpl(false, null);
        typesReferenced.forEach(ti -> {
            String packageName = ti.packageName();
            if (packageName != null && !myPackage.equals(packageName)) {
                boolean doImport = qualification.addTypeReturnImport(ti);
                PerPackage perPackage = typesPerPackage.computeIfAbsent(packageName, p -> new PerPackage());
                if (doImport) {
                    perPackage.types.add(ti);
                } else {
                    perPackage.allowStar = false; // because we don't want to play with complicated ordering
                }
            }
        });
        // IMPROVE static fields and methods
        Set<String> imports = new TreeSet<>();
        for (Map.Entry<String, PerPackage> e : typesPerPackage.entrySet()) {
            PerPackage perPackage = e.getValue();
            if (perPackage.types.size() >= 4 && perPackage.allowStar) {
                imports.add(e.getKey() + ".*");
            } else {
                for (TypeInfo ti : perPackage.types) {
                    imports.add(ti.fullyQualifiedName());
                }
            }
        }
        return new ResultOfImportComputation(imports, qualification);
    }

    private static boolean allowInImport(TypeInfo typeInfo) {
        return !"java.lang".equals(typeInfo.packageName())
               && !typeInfo.isPrimitiveExcludingVoid() && !typeInfo.isVoid();
    }
}
