package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.info.Info;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.analysis.PropertyValueMapImpl;
import org.e2immu.language.cst.impl.variable.DescendModeEnum;
import org.e2immu.support.SetOnce;

import java.util.List;
import java.util.stream.Stream;

public abstract class InfoImpl implements Info {

    private final PropertyValueMap propertyValueMap = new PropertyValueMapImpl();

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
