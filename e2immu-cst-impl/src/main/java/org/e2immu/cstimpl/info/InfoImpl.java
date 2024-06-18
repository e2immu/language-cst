package org.e2immu.cstimpl.info;

import org.e2immu.cstapi.analysis.PropertyValueMap;
import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.analysis.PropertyValueMapImpl;
import org.e2immu.cstimpl.variable.DescendModeEnum;
import org.e2immu.support.SetOnce;

import java.util.List;
import java.util.stream.Stream;

public abstract class InfoImpl implements Element {

    private final PropertyValueMap propertyValueMap = new PropertyValueMapImpl();
    private final SetOnce<List<Comment>> comments = new SetOnce<>();
    private final SetOnce<Source> source = new SetOnce<>();

    @Override
    public List<Comment> comments() {
        return comments.getOrDefaultNull();
    }

    @Override
    public Source source() {
        return source.getOrDefaultNull();
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
        return propertyValueMap;
    }
}
