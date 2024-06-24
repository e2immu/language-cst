package org.e2immu.language.cst.impl.variable;

import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.This;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.ThisNameImpl;
import org.e2immu.language.cst.impl.output.TypeNameImpl;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class ThisImpl extends VariableImpl implements This {

    private final boolean writeSuper;
    private final TypeInfo explicitlyWriteType;
    private final String fullyQualifiedName;

    public ThisImpl(TypeInfo typeInfo) {
        this(typeInfo, null, false);
    }

    public ThisImpl(TypeInfo typeInfo, TypeInfo explicitlyWriteType, boolean writeSuper) {
        super(typeInfo.asSimpleParameterizedType());
        this.writeSuper = writeSuper;
        this.explicitlyWriteType = explicitlyWriteType;
        this.fullyQualifiedName = typeInfo.fullyQualifiedName() + ".this";
    }

    @Override
    public TypeInfo typeInfo() {
        return parameterizedType().typeInfo();
    }

    @Override
    public TypeInfo explicitlyWriteType() {
        return explicitlyWriteType;
    }

    @Override
    public boolean writeSuper() {
        return writeSuper;
    }

    @Override
    public String fullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public String simpleName() {
        String superOrThis = writeSuper ? "super" : "this";
        if (explicitlyWriteType != null) return explicitlyWriteType.simpleName() + "+" + superOrThis;
        return superOrThis;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public int complexity() {
        return 1;
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
        return new OutputBuilderImpl().add(new ThisNameImpl(writeSuper,
                TypeNameImpl.typeName(typeInfo(), qualification.qualifierRequired(typeInfo())),
                qualification.qualifierRequired(this)));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.of(this);
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        return Stream.of(new ElementImpl.TypeReference(typeInfo(), explicitlyWriteType != null));
    }
}
