package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.output.TypeNameRequired;
import org.e2immu.language.cst.api.statement.ReturnStatement;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.type.DiamondEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MethodPrinterImpl(MethodInfo methodInfo) {

    public MethodPrinterImpl(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    public OutputBuilder print(Qualification qualification) {
        if (methodInfo.isStaticBlock()) {
            OutputBuilder result = new OutputBuilderImpl().add(KeywordImpl.STATIC);
            Qualification bodyQualification = makeBodyQualification(qualification);
            result.add(methodInfo.methodBody().print(bodyQualification));
            return result;
        }
        OutputBuilder builder = new OutputBuilderImpl();
        for (AnnotationExpression annotation : methodInfo.annotations()) {
            builder.add(annotation.print(qualification));
            builder.add(SpaceEnum.ONE);
        }
        List<MethodModifier> modifiers = minimalModifiers();
        builder.add(modifiers.stream()
                .map(mod -> new OutputBuilderImpl().add(mod.keyword()))
                .collect(OutputBuilderImpl.joining(SpaceEnum.ONE)));
        if (!modifiers.isEmpty()) builder.add(SpaceEnum.ONE);

        if (!methodInfo.typeParameters().isEmpty()) {
            builder.add(SymbolEnum.LEFT_ANGLE_BRACKET);
            builder.add(methodInfo.typeParameters().stream()
                    .map(tp -> tp.print(qualification, new HashSet<>()))
                    .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)));
            builder.add(SymbolEnum.RIGHT_ANGLE_BRACKET).add(SpaceEnum.ONE);
        }

        if (!methodInfo.isConstructor()) {
            builder.add(methodInfo.returnType().print(qualification, false, DiamondEnum.SHOW_ALL))
                    .add(SpaceEnum.ONE);
        }
        builder.add(new TextImpl(methodInfo.name()));
        if (!methodInfo.isCompactConstructor()) {
            if (methodInfo.parameters().isEmpty()) {
                builder.add(SymbolEnum.OPEN_CLOSE_PARENTHESIS);
            } else {
                builder.add(methodInfo.parameters().stream()
                        .map(pi -> outputDeclaration(pi, qualification))
                        .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA, SymbolEnum.LEFT_PARENTHESIS, SymbolEnum.RIGHT_PARENTHESIS,
                                GuideImpl.generatorForParameterDeclaration())));
            }
        }
        if (!methodInfo.exceptionTypes().isEmpty()) {
            builder.add(SpaceEnum.ONE_REQUIRED_EASY_SPLIT).add(KeywordImpl.THROWS).add(SpaceEnum.ONE)
                    .add(methodInfo.exceptionTypes().stream()
                            .map(pi -> pi.print(qualification, false, DiamondEnum.SHOW_ALL))
                            .collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)));
        }
        if (methodInfo.isAbstract()) {
            builder.add(SymbolEnum.SEMICOLON);
        } else if (methodInfo.typeInfo().typeNature().isAnnotation()) {
            // default value when the method is not abstract
            OutputBuilder expression = methodInfo.methodBody().asInstanceOf(ReturnStatement.class).expression()
                    .print(qualification);
            builder.add(SpaceEnum.ONE)
                    .add(new TextImpl(KeywordImpl.DEFAULT.minimal()))
                    .add(SpaceEnum.ONE)
                    .add(expression).add(SymbolEnum.SEMICOLON);
        } else {
            Qualification bodyQualification = makeBodyQualification(qualification);
            builder.add(methodInfo.methodBody().print(bodyQualification));
        }
        return builder;
    }

    private List<MethodModifier> minimalModifiers() {
        List<MethodModifier> result = new ArrayList<>();
        Access access = methodInfo.access();
        boolean inInterface = methodInfo.typeInfo().isInterface();
        boolean inAnnotation = methodInfo.typeInfo().isAnnotation();
        boolean isAbstract = methodInfo.isAbstract();
        boolean isDefault = methodInfo.isDefault();
        if (!access.isPackage() && !(inInterface && (isAbstract || isDefault))) {
            MethodModifier accessModifier;
            if (access.isPrivate()) accessModifier = MethodModifierEnum.PRIVATE;
            else if (access.isProtected()) accessModifier = MethodModifierEnum.PROTECTED;
            else if (access.isPublic()) accessModifier = MethodModifierEnum.PUBLIC;
            else throw new UnsupportedOperationException();

            result.add(accessModifier);
        }
        if (inInterface || inAnnotation) {
            if (isDefault) result.add(MethodModifierEnum.DEFAULT);
        } else {
            if (isAbstract) result.add(MethodModifierEnum.ABSTRACT);
        }
        if (methodInfo.isStatic()) result.add(MethodModifierEnum.STATIC);
        if (methodInfo.isFinal()) result.add(MethodModifierEnum.FINAL);
        if (methodInfo.isSynchronized()) result.add(MethodModifierEnum.SYNCHRONIZED);
        return result;
    }

    private Qualification makeBodyQualification(Qualification qualification) {
        if (qualification instanceof QualificationImpl qi) {
            Set<String> localNamesFromBody = methodInfo.variableStreamDescend()
                    .filter(v -> v instanceof LocalVariable || v instanceof ParameterInfo)
                    .map(Variable::simpleName).collect(Collectors.toSet());
            Set<String> parameterNames = methodInfo.parameters().stream()
                    .map(ParameterInfo::simpleName).collect(Collectors.toSet());
            Set<String> localNames = Stream.concat(localNamesFromBody.stream(), parameterNames.stream())
                    .collect(Collectors.toUnmodifiableSet());

            List<FieldInfo> visibleFields = methodInfo.typeInfo().fields();
            QualificationImpl res = new QualificationImpl(qi.doNotQualifyImplicit(), qi,
                    TypeNameImpl.Required.QUALIFIED_FROM_PRIMARY_TYPE);
            visibleFields.stream().filter(fieldInfo -> !localNames.contains(fieldInfo.name())).forEach(res::addField);

            return res;
        }
        return qualification;
    }

    private OutputBuilder outputDeclaration(ParameterInfo pi, Qualification qualification) {
        OutputBuilder outputBuilder = new OutputBuilderImpl();
        for (AnnotationExpression annotation : pi.annotations()) {
            outputBuilder.add(annotation.print(qualification));
            outputBuilder.add(SpaceEnum.ONE);
        }
        if (!pi.parameterizedType().isNoTypeGivenInLambda()) {
            outputBuilder.add(pi.parameterizedType().print(qualification, pi.isVarArgs(), DiamondEnum.SHOW_ALL));
            outputBuilder.add(SpaceEnum.ONE);
        }
        outputBuilder.add(new TextImpl(pi.name()));
        return outputBuilder;
    }
}
