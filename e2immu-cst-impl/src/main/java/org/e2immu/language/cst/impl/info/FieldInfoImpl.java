package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.Access;
import org.e2immu.language.cst.api.info.FieldInfo;
import org.e2immu.language.cst.api.info.FieldModifier;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.type.DiamondEnum;
import org.e2immu.support.EventuallyFinal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.e2immu.language.cst.impl.info.InspectionImpl.AccessEnum.PRIVATE;
import static org.e2immu.language.cst.impl.info.InspectionImpl.AccessEnum.PUBLIC;

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

    public OutputBuilder print(Qualification qualification, boolean asParameter) {
        Stream<OutputBuilder> annotationStream = annotations().stream().map(ae -> ae.print(qualification));

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

        return Stream.concat(annotationStream, Stream.of(outputBuilder))
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
        return Stream.concat(type.typesReferenced().map(t -> new ElementImpl.TypeReference(t.typeInfo(), true)),
                initializer == null ? Stream.of() : initializer.typesReferenced());
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
    public boolean isSynthetic() {
        return inspection.get().isSynthetic();
    }

    @Override
    public boolean hasBeenInspected() {
        return inspection.isFinal();
    }

    @Override
    public List<FieldInfo> translate(TranslationMap translationMap) {
        return List.of(this);
    }
}
