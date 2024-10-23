package org.e2immu.language.cst.impl.variable;

import org.e2immu.annotation.NotNull;
import org.e2immu.annotation.Nullable;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.TypeExpression;
import org.e2immu.language.cst.api.expression.VariableExpression;
import org.e2immu.language.cst.api.info.FieldInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.output.element.TypeName;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.FieldReference;
import org.e2immu.language.cst.api.variable.This;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.language.cst.impl.expression.TypeExpressionImpl;
import org.e2immu.language.cst.impl.expression.VariableExpressionImpl;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.type.DiamondEnum;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
        this(fieldInfo, null, null, fieldInfo.type());
    }

    public FieldReferenceImpl(FieldInfo fieldInfo,
                              Expression scope,
                              Variable overrideScopeVariable,
                              ParameterizedType parameterizedType) {
        super(parameterizedType);
        this.fieldInfo = Objects.requireNonNull(fieldInfo);
        // NOTE: for the sake of translations: if the scope is given, we take it, even if the result may not compile
        if (fieldInfo.isStatic()) {
            this.scope = scope == null
                    ? new TypeExpressionImpl(fieldInfo.owner().asSimpleParameterizedType(), DiamondEnum.NO)
                    : scope;
            isDefaultScope = scope instanceof TypeExpression te && fieldInfo.owner() == te.parameterizedType().typeInfo();
            this.scopeVariable = null;
        } else if (scope == null) {
            scopeVariable = new ThisImpl(fieldInfo.owner().asParameterizedType());
            this.scope = new VariableExpressionImpl(scopeVariable);
            isDefaultScope = true;
        } else {
            this.scope = scope;
            if (scope instanceof VariableExpression ve) {
                scopeVariable = ve.variable();
                isDefaultScope = ve.variable() instanceof This thisVar && thisVar.typeInfo() == fieldInfo.owner();
            } else {
                // the scope is not a variable, we must introduce a new scope variable
                isDefaultScope = false;
                scopeVariable = overrideScopeVariable;
            }
        }
        this.fullyQualifiedName = computeFqn(scope);
        assert !(scopeIsRecursivelyThis() && fieldInfo.isStatic());
        // know that: assert this.scope != null;
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
    public boolean isStatic() {
        return fieldInfo.isStatic();
    }

    private String computeFqn(Expression scope) {
        if (isStatic() || scopeIsThis()) {
            return fieldInfo.fullyQualifiedName();
        }
        String scopeFqn;
        if (scopeVariable != null) {
            scopeFqn = scopeVariable.fullyQualifiedName();
        } else {
            int unique = Math.abs(scope.hashCode());
            scopeFqn = "scope" + unique;
        }
        return fieldInfo.fullyQualifiedName() + "#" + scopeFqn;
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
            TypeName typeName = TypeNameImpl.typeName(thisVar.typeInfo(),
                    qualification.qualifierRequired(thisVar.typeInfo()), false);
            ThisNameImpl thisName = new ThisNameImpl(thisVar.writeSuper(),
                    typeName,
                    qualification.qualifierRequired(thisVar));
            return new OutputBuilderImpl().add(new QualifiedNameImpl(fieldInfo.name(), thisName,
                    qualification.qualifierRequired(this) ? QualifiedNameImpl.Required.YES : QualifiedNameImpl.Required.NO_FIELD));
        }
        if (qualification.isSimpleOnly()) {
            return new OutputBuilderImpl().add(new QualifiedNameImpl(simpleName(), null, QualifiedNameImpl.Required.NEVER));
        }
        if (isStatic()) {
            TypeName typeName = TypeNameImpl.typeName(fieldInfo.typeInfo(),
                    qualification.qualifierRequired(fieldInfo.typeInfo()), false);
            QualifiedNameImpl.Required required = qualification.qualifierRequired(this)
                    ? QualifiedNameImpl.Required.YES : QualifiedNameImpl.Required.NO_FIELD;
            return new OutputBuilderImpl().add(new QualifiedNameImpl(fieldInfo.name(), typeName, required));
        }
        // real variable
        return new OutputBuilderImpl().add(scope.print(qualification)).add(SymbolEnum.DOT)
                .add(new QualifiedNameImpl(simpleName(), null, QualifiedNameImpl.Required.NEVER));
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
        if (scope != null) {
            Stream<TypeReference> nonDefault = isDefaultScope ? Stream.of()
                    : Stream.of(new ElementImpl.TypeReference(fieldInfo.owner(), true));
            return Stream.concat(nonDefault, Stream.concat(scope.typesReferenced(), parameterizedType().typesReferenced()));
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
    public boolean scopeIsRecursively(Variable variable) {
        if (variable.equals(scopeVariable)) return true;
        if (scope instanceof VariableExpression ve) {
            return ve.variable().scopeIsRecursively(variable);
        }
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
