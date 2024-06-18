package org.e2immu.cstimpl.variable;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.output.QualificationImpl;

import java.util.List;
import java.util.stream.Stream;

public abstract class VariableImpl implements Variable {

    private final ParameterizedType parameterizedType;

    public VariableImpl(ParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    @Override
    public ParameterizedType parameterizedType() {
        return parameterizedType;
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
    public Stream<Variable> variableStreamDescend() {
        return variables(DescendModeEnum.YES);
    }

    @Override
    public Stream<Variable> variableStreamDoNotDescend() {
        return variables(DescendModeEnum.NO);
    }

    @Override
    public String toString() {
        return print(QualificationImpl.FULLY_QUALIFIED_NAMES).toString();
    }
}
