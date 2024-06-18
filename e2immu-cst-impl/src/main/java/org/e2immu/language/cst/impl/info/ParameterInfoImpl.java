package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.info.ParameterInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.analysis.PropertyImpl;
import org.e2immu.language.cst.impl.analysis.PropertyValueMapImpl;
import org.e2immu.language.cst.impl.analysis.ValueImpl;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.TextImpl;
import org.e2immu.language.cst.impl.variable.DescendModeEnum;
import org.e2immu.support.EventuallyFinal;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ParameterInfoImpl implements ParameterInfo {
    private final int index;
    private final String name;
    private final MethodInfo methodInfo;
    private final ParameterizedType parameterizedType;
    private final EventuallyFinal<ParameterInspection> inspection;
    private final PropertyValueMap analysis = new PropertyValueMapImpl();

    public ParameterInfoImpl(MethodInfo methodInfo, int index, String name, ParameterizedType parameterizedType) {
        this.methodInfo = methodInfo;
        this.index = index;
        this.name = name;
        this.parameterizedType = parameterizedType;
        inspection = new EventuallyFinal<>();
        inspection.setVariable(new ParameterInspectionImpl.Builder(this));
    }

    public ParameterInspectionImpl.Builder inspectionBuilder() {
        if (inspection.isVariable()) return (ParameterInspectionImpl.Builder) inspection.get();
        throw new UnsupportedOperationException();
    }

    @Override
    public Builder builder() {
        if (inspection.isVariable()) return (ParameterInfo.Builder) inspection.get();
        throw new UnsupportedOperationException();
    }

    public boolean hasBeenCommitted() {
        return inspection.isFinal();
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isVarArgs() {
        return inspection.get().isVarArgs();
    }

    @Override
    public String fullyQualifiedName() {
        return methodInfo.fullyQualifiedName() + ":" + index + ":" + name;
    }

    @Override
    public String simpleName() {
        return name;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return parameterizedType;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public int complexity() {
        return 0;
    }

    @Override
    public List<Comment> comments() {
        return List.of();
    }

    @Override
    public Source source() {
        return null;
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        predicate.test(this);
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.beforeVariable(this);
        visitor.afterVariable(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl().add(new TextImpl(simpleName()));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.of(this);
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        return Stream.empty();
    }

    @Override
    public Stream<Variable> variableStreamDescend() {
        return variables(DescendModeEnum.YES);
    }

    @Override
    public Stream<Variable> variableStreamDoNotDescend() {
        return variables(DescendModeEnum.NO);
    }

    @Override
    public PropertyValueMap analysis() {
        return analysis;
    }

    @Override
    public boolean isModified() {
        return analysis.getOrDefault(PropertyImpl.MODIFIED_PARAMETER, ValueImpl.BoolImpl.TRUE).isTrue();
    }

    @Override
    public boolean isIgnoreModifications() {
        return analysis.getOrDefault(PropertyImpl.IGNORE_MODIFICATIONS_PARAMETER, ValueImpl.BoolImpl.FALSE).isTrue();
    }

    @Override
    public Value.AssignedToField assignedToField() {
        return analysis.getOrDefault(PropertyImpl.PARAMETER_ASSIGNED_TO_FIELD, ValueImpl.AssignedToFieldImpl.EMPTY);
    }

    public void commit(ParameterInspection pi) {
        inspection.setFinal(pi);
    }

}
