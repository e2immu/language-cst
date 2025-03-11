package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.type.DiamondEnum;
import org.e2immu.support.EventuallyFinal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FieldInfoImpl extends InfoImpl implements FieldInfo {

    private final String name;
    private final boolean isStatic;
    private final ParameterizedType type;
    private final String fullyQualifiedName;
    private final TypeInfo owner;
    private final EventuallyFinal<FieldInspection> inspection;

    public FieldInfoImpl(String name, boolean isStatic, ParameterizedType type, TypeInfo owner) {
        this.name = name;
        this.isStatic = isStatic;
        this.type = type;
        this.fullyQualifiedName = owner.fullyQualifiedName() + "." + name;
        this.owner = owner;
        inspection = new EventuallyFinal<>();
        inspection.setVariable(new FieldInspectionImpl.Builder(this));
    }

    @Override
    public String info() {
        return "field";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldInfoImpl fieldInfo)) return false;
        return Objects.equals(fullyQualifiedName, fieldInfo.fullyQualifiedName);
    }

    @Override
    public String toString() {
        return fullyQualifiedName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fullyQualifiedName);
    }

    public boolean hasBeenCommitted() {
        return inspection.isFinal();
    }

    @Override
    public TypeInfo typeInfo() {
        return owner;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String simpleName() {
        return name;
    }

    @Override
    public TypeInfo owner() {
        return owner;
    }

    @Override
    public ParameterizedType type() {
        return type;
    }

    @Override
    public String fullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isFinal() {
        return inspection.get().fieldModifiers().contains(FieldModifierEnum.FINAL);
    }

    @Override
    public boolean isTransient() {
        return inspection.get().fieldModifiers().contains(FieldModifierEnum.TRANSIENT);
    }

    @Override
    public boolean isVolatile() {
        return inspection.get().fieldModifiers().contains(FieldModifierEnum.VOLATILE);
    }

    @Override
    public List<AnnotationExpression> annotations() {
        return inspection.get().annotations();
    }

    @Override
    public boolean isPropertyNotNull() {
        if (type.isPrimitiveExcludingVoid()) return true;
        return analysis().getOrDefault(PropertyImpl.NOT_NULL_FIELD, ValueImpl.NotNullImpl.NULLABLE).isAtLeastNotNull();
    }

    @Override
    public Access access() {
        return inspection.get().access();
    }

    @Override
    public boolean isPropertyFinal() {
        if (isFinal()) return true;
        return analysis().getOrDefault(PropertyImpl.FINAL_FIELD, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public boolean isIgnoreModifications() {
        return analysis().getOrDefault(PropertyImpl.IGNORE_MODIFICATIONS_FIELD, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public int complexity() {
        return 1 + inspection.get().initializer().complexity();
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
        return print(qualification, false);
    }

    @Override
    public OutputBuilder print(Qualification qualification, boolean asParameter) {
        Stream<OutputBuilder> annotationStream = Stream.concat(annotations().stream(),
                        qualification.decorator() == null ? Stream.of()
                                : qualification.decorator().annotations(this).stream())
                .map(ae -> ae.print(qualification));

        OutputBuilder outputBuilder = new OutputBuilderImpl();
        if (hasBeenCommitted() && !asParameter) {
            List<FieldModifier> fieldModifiers = minimalModifiers();
            outputBuilder.add(fieldModifiers.stream()
                    .map(mod -> new OutputBuilderImpl().add(mod.keyword()))
                    .collect(OutputBuilderImpl.joining(SpaceEnum.ONE)));
            if (!fieldModifiers.isEmpty()) outputBuilder.add(SpaceEnum.ONE);
        }
        outputBuilder
                .add(type.print(qualification, false, DiamondEnum.SHOW_ALL))
                .add(SpaceEnum.ONE)
                .add(new TextImpl(name));
        if (!asParameter && initializer() != null && !initializer().isEmpty()) {
            outputBuilder.add(SymbolEnum.assignment("=")).add(initializer().print(qualification));
        }
        if (!asParameter) {
            outputBuilder.add(SymbolEnum.SEMICOLON);
        }
        Stream<Comment> commentStream;
        if (qualification.decorator() != null) {
            commentStream = Stream.concat(comments().stream(), qualification.decorator().comments(this).stream());
        } else {
            commentStream = comments().stream();
        }
        Stream<OutputBuilder> commentOBStream = commentStream.map(c -> c.print(qualification));
        return Stream.concat(Stream.concat(commentOBStream, annotationStream), Stream.of(outputBuilder))
                .collect(OutputBuilderImpl.joining(SpaceEnum.ONE_REQUIRED_EASY_SPLIT,
                        GuideImpl.generatorForAnnotationList()));
    }

    private static FieldModifier toFieldModifier(Access access) {
        if (access.isPublic()) return FieldModifierEnum.PUBLIC;
        if (access.isPrivate()) return FieldModifierEnum.PRIVATE;
        if (access.isProtected()) return FieldModifierEnum.PROTECTED;
        throw new UnsupportedOperationException();
    }


    private List<FieldModifier> minimalModifiers() {
        Set<FieldModifier> modifiers = modifiers();
        List<FieldModifier> list = new ArrayList<>();
        Access access = access();
        Access ownerAccess = owner.access();

        /*
        if the owner access is private, we don't write any modifier
         */
        if (access.le(ownerAccess) && !access.isPackage() && !ownerAccess.isPrivate()) {
            list.add(toFieldModifier(access));
        }
        // sorting... STATIC, FINAL, VOLATILE, TRANSIENT
        boolean inInterface = owner.isInterface();
        if (!inInterface) {
            if (modifiers.contains(FieldModifierEnum.STATIC)) {
                list.add(FieldModifierEnum.STATIC);
            }
            if (modifiers.contains(FieldModifierEnum.FINAL)) {
                list.add(FieldModifierEnum.FINAL);
            }
        }
        if (modifiers.contains(FieldModifierEnum.VOLATILE)) {
            assert !inInterface;
            list.add(FieldModifierEnum.VOLATILE);
        }
        if (modifiers.contains(FieldModifierEnum.TRANSIENT)) {
            assert !inInterface;
            list.add(FieldModifierEnum.TRANSIENT);
        }

        return list;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        Expression initializer = inspection.get().initializer();
        Stream<TypeReference> fromAnnotations = annotations().stream().flatMap(Element::typesReferenced);
        Stream<TypeReference> fromInitializer = initializer == null ? Stream.of() : initializer.typesReferenced();
        return Stream.concat(fromAnnotations,
                Stream.concat(type.typesReferencedMadeExplicit(), fromInitializer));
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
    public FieldInfo.Builder builder() {
        assert inspection.isVariable();
        return (FieldInfo.Builder) inspection.get();
    }

    public void commit(FieldInspectionImpl fieldInspection) {
        inspection.setFinal(fieldInspection);
    }

    @Override
    public Set<FieldModifier> modifiers() {
        return inspection.get().fieldModifiers();
    }

    @Override
    public Expression initializer() {
        return inspection.get().initializer();
    }

    @Override
    public PropertyValueMap analysisOfInitializer() {
        return inspection.get().analysisOfInitializer();
    }

    @Override
    public boolean isSynthetic() {
        return inspection.get().isSynthetic();
    }

    @Override
    public boolean hasBeenInspected() {
        return inspection.isFinal();
    }

    @Override
    public List<FieldInfo> translate(TranslationMap translationMap) {
        Expression init = initializer();
        Expression tInit = init.translate(translationMap);
        TypeInfo tOwner = translationMap.translateType(owner.asSimpleParameterizedType()).typeInfo();
        ParameterizedType tType = translationMap.translateType(type);

        if (tOwner != owner || tInit != init || tType != type || !analysis().isEmpty() && translationMap.isClearAnalysis()) {
            FieldInfoImpl newField = new FieldInfoImpl(name, isStatic, tType, tOwner);
            newField.builder()
                    .setInitializer(tInit)
                    .setSynthetic(isSynthetic());
            modifiers().forEach(newField.builder()::addFieldModifier);
            newField.builder().computeAccess();
            newField.builder().commit();
            if (!translationMap.isClearAnalysis()) {
                newField.analysis().setAll(analysis());
            }
            return List.of(newField);
        }
        return List.of(this);
    }

    @Override
    public FieldInfo withOwnerVariableBuilder(TypeInfo newOwner) {
        FieldInfoImpl fi = new FieldInfoImpl(name, isStatic, type, newOwner);
        fi.inspection.setVariable(new FieldInspectionImpl.Builder(fi, inspection.get()));
        return fi;
    }

    @Override
    public boolean isModified() {
        return analysis().getOrDefault(PropertyImpl.MODIFIED_FIELD, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public void rewirePhase3(InfoMap infoMap) {
        FieldInfo rewiredField = infoMap.fieldInfo(this);
        rewiredField.builder().setInitializer(initializer().rewire(infoMap)).commit();
    }

    @Override
    public Element rewire(InfoMap infoMap) {
        throw new UnsupportedOperationException("Must use the infoMap.fieldInfo() method");
    }
}
