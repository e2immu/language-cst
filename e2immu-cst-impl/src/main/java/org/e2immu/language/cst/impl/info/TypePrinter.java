package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.expression.ConstructorCall;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.type.DiamondEnum;
import org.e2immu.language.cst.impl.variable.ThisImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record TypePrinter(TypeInfo typeInfo) {

    public OutputBuilder print(Qualification qualification, boolean doTypeDeclaration) {
        Set<String> imports;
        Qualification insideType;
        if (typeInfo.isPrimaryType() && typeInfo.hasBeenInspected()) {
            ResultOfImportComputation res = imports(typeInfo.packageName(), typeInfo, qualification);
            imports = res.imports;
            insideType = res.qualification;
        } else {
            imports = Set.of();
            insideType = typeInfo.hasBeenInspected() && qualification instanceof QualificationImpl
                    ? new QualificationImpl(false, qualification, qualification.typeNameRequired())
                    : qualification;
        }
        assert insideType != null;

        // add the methods that we can call without having to qualify (method() instead of super.method())
        if (insideType instanceof QualificationImpl qi) {
            addMethodsToQualification(typeInfo, qi);
            addThisToQualification(typeInfo, qi);
        }

        // PACKAGE AND IMPORTS

        OutputBuilder packageAndImports = new OutputBuilderImpl();
        if (typeInfo.isPrimaryType()) {
            String packageName = typeInfo.packageName();
            if (!packageName.isEmpty()) {
                packageAndImports.add(KeywordImpl.PACKAGE).add(SpaceEnum.ONE).add(new TextImpl(packageName))
                        .add(SymbolEnum.SEMICOLON)
                        .add(SpaceEnum.NEWLINE);
            }
            imports.stream().sorted().forEach(i -> packageAndImports.add(KeywordImpl.IMPORT).add(SpaceEnum.ONE)
                    .add(new TextImpl(i)).add(SymbolEnum.SEMICOLON).add(SpaceEnum.NEWLINE));
        }

        OutputBuilder afterAnnotations = new OutputBuilderImpl();
        if (doTypeDeclaration) {
            // the class name
            afterAnnotations
                    .add(minimalModifiers(typeInfo).stream().map(mod -> new OutputBuilderImpl().add(mod.keyword()))
                            .collect(OutputBuilderImpl.joining(SpaceEnum.ONE)))
                    .add(SpaceEnum.ONE).add(typeInfo.typeNature().keyword())
                    .add(SpaceEnum.ONE).add(new TextImpl(typeInfo.simpleName()));

            if (!typeInfo.typeParameters().isEmpty()) {
                afterAnnotations.add(SymbolEnum.LEFT_ANGLE_BRACKET);
                afterAnnotations.add(typeInfo.typeParameters().stream().map(tp ->
                                tp.print(insideType, new HashSet<>()))
                        .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)));
                afterAnnotations.add(SymbolEnum.RIGHT_ANGLE_BRACKET);
            }
            if (typeInfo.typeNature().isRecord()) {
                afterAnnotations.add(outputNonStaticFieldsAsParameters(insideType, typeInfo.fields()));
            }
            if (typeInfo.parentClass() != null && !typeInfo.parentClass().isJavaLangObject()) {
                afterAnnotations.add(SpaceEnum.ONE).add(KeywordImpl.EXTENDS).add(SpaceEnum.ONE)
                        .add(typeInfo.parentClass().print(insideType, false, DiamondEnum.SHOW_ALL));
            }
            if (!typeInfo.interfacesImplemented().isEmpty()) {
                afterAnnotations.add(SpaceEnum.ONE)
                        .add(typeInfo.isInteger() ? KeywordImpl.EXTENDS : KeywordImpl.IMPLEMENTS)
                        .add(SpaceEnum.ONE);
                afterAnnotations.add(typeInfo.interfacesImplemented().stream()
                        .map(pi -> pi.print(insideType, false, DiamondEnum.SHOW_ALL))
                        .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)));
            }
        }

        OutputBuilder main = Stream.concat(Stream.concat(Stream.concat(Stream.concat(
                                                enumConstantStream(typeInfo, insideType),
                                                typeInfo.fields().stream()
                                                        .filter(f -> !f.isSynthetic())
                                                        .map(f -> f.print(insideType))),
                                        typeInfo.subTypes().stream()
                                                .filter(st -> !st.isSynthetic())
                                                .map(ti -> ti.print(insideType))),
                                typeInfo.constructors().stream()
                                        .filter(c -> !c.isSynthetic())
                                        .map(c -> c.print(insideType))),
                        typeInfo.methods().stream()
                                .filter(m -> !m.isSynthetic())
                                .map(m -> m.print(insideType)))
                .collect(OutputBuilderImpl.joining(SpaceEnum.NONE, SymbolEnum.LEFT_BRACE, SymbolEnum.RIGHT_BRACE,
                        GuideImpl.generatorForBlock()));
        afterAnnotations.add(main);

        // annotations and the rest of the type are at the same level
        Stream<OutputBuilder> annotationStream = doTypeDeclaration
                ? typeInfo.annotations().stream().map(ae -> ae.print(qualification))
                : Stream.of();
        if (typeInfo().comments() != null) {
            typeInfo.comments().forEach(c -> packageAndImports.add(c.print(qualification)));
        }
        return packageAndImports.add(Stream.concat(annotationStream, Stream.of(afterAnnotations))
                .collect(OutputBuilderImpl.joining(SpaceEnum.ONE_REQUIRED_EASY_SPLIT,
                        GuideImpl.generatorForAnnotationList())));
    }

    private static List<TypeModifier> minimalModifiers(TypeInfo typeInfo) {
        Set<TypeModifier> modifiers = typeInfo.typeModifiers();
        List<TypeModifier> list = new ArrayList<>();

        // access
        Access access = typeInfo.access();
        Access enclosedAccess = typeInfo.compilationUnitOrEnclosingType().isLeft()
                ? InspectionImpl.AccessEnum.PUBLIC
                : typeInfo.compilationUnitOrEnclosingType().getRight().access();
        if (!enclosedAccess.isPrivate() && !access.isPackage()) {
            list.add(typeModifier(access));
        } // else there really is no point anymore to show any access modifier, let's keep it brief

        // 'abstract', 'static'
        if (typeInfo.typeNature().isClass()) {
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
        if (access.isPublic()) return TypeModifierEnum.PUBLIC;
        if (access.isProtected()) return TypeModifierEnum.PROTECTED;
        if (access.isPrivate()) return TypeModifierEnum.PRIVATE;
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

    private static ResultOfImportComputation imports(String myPackage, TypeInfo typeInfo, Qualification q) {
        Set<TypeInfo> typesReferenced = typeInfo.typesReferenced()
                .filter(Element.TypeReference::explicit)
                .map(Element.TypeReference::typeInfo)
                .map(TypeInfo::primaryType)
                .filter(TypePrinter::allowInImport)
                .collect(Collectors.toSet());
        Map<String, PerPackage> typesPerPackage = new HashMap<>();
        QualificationImpl qualification = new QualificationImpl(q.doNotQualifyImplicit(), q.typeNameRequired());
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
