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
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.e2immu.support.EventuallyFinal;

import java.util.List;
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

    public boolean hasBeenCommitted() {
        return inspection.isFinal();
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
        return analysis().getOrDefault(PropertyImpl.NOT_NULL_FIELD, ValueImpl.BoolImpl.FALSE).isTrue();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        Expression initializer = inspection.get().initializer();
        return Stream.concat(type.typesReferenced(), initializer == null ? Stream.of() : initializer.typesReferenced());
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
}
