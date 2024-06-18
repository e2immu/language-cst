package org.e2immu.cstimpl.variable;

import org.e2immu.annotation.NotNull;
import org.e2immu.annotation.Nullable;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.expression.VariableExpression;
import org.e2immu.cstapi.info.FieldInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.output.element.TypeName;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.FieldReference;
import org.e2immu.cstapi.variable.This;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.expression.TypeExpressionImpl;
import org.e2immu.cstimpl.expression.VariableExpressionImpl;
import org.e2immu.cstimpl.output.*;
import org.e2immu.cstimpl.type.DiamondEnum;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.e2immu.cstimpl.output.QualifiedNameImpl.Required.*;

public class FieldReferenceImpl extends VariableImpl implements FieldReference {
    @NotNull
    private final FieldInfo fieldInfo;

    @NotNull
    private final Expression scope;

    @Nullable
    private final Variable scopeVariable;

    private final boolean isDefaultScope;

    @NotNull
    private final String fullyQualifiedName;

    public FieldReferenceImpl(FieldInfo fieldInfo) {
        this(fieldInfo, null, null, null);
    }

    public FieldReferenceImpl(FieldInfo fieldInfo,
                              Expression scope,
                              Variable overrideScopeVariable,
                              ParameterizedType parameterizedType) {
        super(parameterizedType);
        this.fieldInfo = Objects.requireNonNull(fieldInfo);
        if (fieldInfo.isStatic()) {
            // IMPORTANT: the owner doesn't necessarily have a decent identifier, but the field should have one
            this.scope = new TypeExpressionImpl(fieldInfo.owner().asSimpleParameterizedType(), DiamondEnum.NO);
            isDefaultScope = true;
            this.scopeVariable = null;
        } else if (scope == null) {
            scopeVariable = new ThisImpl(fieldInfo.owner());
            this.scope = new VariableExpressionImpl(scopeVariable);
            isDefaultScope = true;
        } else {
            if (scope instanceof VariableExpression ve) {
                if (ve.variable() instanceof This thisVar) {
                    if (thisVar.typeInfo() == fieldInfo.owner()) {
                        this.scope = scope;
                        scopeVariable = ve.variable();
                    } else {
                        scopeVariable = new ThisImpl(fieldInfo.owner());
                        this.scope = new VariableExpressionImpl(scopeVariable);
                    }
                    isDefaultScope = true;
                } else {
                    this.scope = scope;
                    isDefaultScope = false;
                    scopeVariable = ve.variable();
                }
            } else {
                // the scope is not a variable, we must introduce a new scope variable
                this.scope = scope;
                isDefaultScope = false;
                scopeVariable = overrideScopeVariable != null ? overrideScopeVariable : newScopeVariable(scope);
            }
        }
        this.fullyQualifiedName = computeFqn();
        assert (scopeVariable == null) == fieldInfo.isStatic();
    }

    protected Variable newScopeVariable(Expression scope) {
        int unique = Math.abs(scope.hashCode());
        String name = "scope" + unique;
        return new LocalVariableImpl(name, scope.parameterizedType(), scope);
    }

    @Override
    public String fullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public String simpleName() {
        return fieldInfo.name();
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return fieldInfo.isStatic();
    }

    private String computeFqn() {
        if (isStatic() || scopeIsThis()) {
            return fieldInfo.fullyQualifiedName();
        }
        return fieldInfo.fullyQualifiedName() + "#" + scopeVariable.fullyQualifiedName();
    }

    @Override
    public int complexity() {
        if (isStatic()) return 2;
        return 1 + scope.complexity();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (scope != null) scope.visit(predicate);
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeVariable(this)) {
            if (scope != null) {
                scope.visit(visitor);
            }
        }
        visitor.afterVariable(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        if (scope instanceof VariableExpression ve && ve.variable() instanceof This thisVar) {
            TypeName typeName = TypeNameImpl.typeName(thisVar.typeInfo(), qualification.qualifierRequired(thisVar.typeInfo()));
            ThisNameImpl thisName = new ThisNameImpl(thisVar.writeSuper(),
                    typeName,
                    qualification.qualifierRequired(thisVar));
            return new OutputBuilderImpl().add(new QualifiedNameImpl(fieldInfo.name(), thisName,
                    qualification.qualifierRequired(this) ? YES : NO_FIELD));
        }
        // real variable
        return new OutputBuilderImpl().add(scope.print(qualification)).add(SymbolEnum.DOT)
                .add(new QualifiedNameImpl(simpleName(), null, NEVER));
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        if (descendMode.isYes() && scopeVariable != null) {
            return Stream.concat(Stream.of(this), scopeVariable.variables(descendMode));
        }
        return Stream.of(this);
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        if (scope != null && !scopeIsThis()) {
            return Stream.concat(scope.typesReferenced(), parameterizedType().typesReferenced());
        }
        return parameterizedType().typesReferenced();
    }

    @Override
    public FieldInfo fieldInfo() {
        return fieldInfo;
    }

    @Override
    public Expression scope() {
        return scope;
    }

    @Override
    public Variable scopeVariable() {
        return scopeVariable;
    }

    @Override
    public boolean scopeIsRecursivelyThis() {
        if (scopeIsThis()) return true;
        if (scopeVariable instanceof FieldReference fr) return fr.scopeIsRecursivelyThis();
        return false;
    }

    @Override
    public boolean isDefaultScope() {
        return isDefaultScope;
    }

    @Override
    public boolean scopeIsThis() {
        return scopeVariable instanceof This;
    }
}
