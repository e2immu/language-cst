package org.e2immu.language.cst.impl.variable;

import org.e2immu.annotation.NotNull;
import org.e2immu.annotation.Nullable;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.VariableExpression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.DependentVariable;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SymbolEnum;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DependentVariableImpl extends VariableImpl implements DependentVariable {
    @NotNull
    private final Expression indexExpression;

    @NotNull
    private final Expression arrayExpression;

    @Nullable
    private final Variable arrayVariable;

    @Nullable
    private final Variable indexVariable;

    private final String simpleName;
    private final String fullyQualifiedName;

    public static final String ARRAY_VARIABLE = "av-";
    public static final String INDEX_VARIABLE = "iv-";

    public static DependentVariable create(Expression array, Expression index) {
        Variable av = makeVariable(array, ARRAY_VARIABLE);
        Variable iv = makeVariable(index, INDEX_VARIABLE);
        ParameterizedType pt = array.parameterizedType().copyWithOneFewerArrays();
        return new DependentVariableImpl(array, Objects.requireNonNull(av), index, iv, pt);
    }

    public static Variable makeVariable(Expression expression, String variablePrefix) {
        if (expression.isConstant()) return null;
        VariableExpression ve;
        if ((ve = expression.asInstanceOf(VariableExpression.class)) != null) {
            return ve.variable();
        }
        Source source = expression.source();
        assert source != null : "Expression " + expression.getClass() + " has no source";
        String name = variablePrefix + source.compact();
        return new LocalVariableImpl(name, expression.parameterizedType(), null);
    }


    public DependentVariableImpl(Expression arrayExpression,
                                 Variable arrayVariable,
                                 Expression indexExpression,
                                 Variable indexVariable,
                                 ParameterizedType parameterizedType) {
        super(parameterizedType);
        this.indexExpression = indexExpression;
        this.arrayExpression = arrayExpression;
        this.arrayVariable = arrayVariable;
        this.indexVariable = indexVariable;
        String indexFqn = indexVariable == null ? indexExpression.print(null).toString() : indexVariable.fullyQualifiedName();
        fullyQualifiedName = arrayVariable.fullyQualifiedName() + "[" + indexFqn + "]";
        String indexSimple = indexVariable == null ? indexExpression.print(null).toString() : indexVariable.simpleName();
        simpleName = arrayVariable.simpleName() + "[" + indexSimple + "]";
    }

    @Override
    public Variable arrayVariable() {
        return arrayVariable;
    }

    @Override
    public int complexity() {
        return arrayExpression.complexity() + indexExpression.complexity();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            arrayExpression.visit(predicate);
            indexExpression.visit(predicate);
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeVariable(this)) {
            arrayExpression.visit(visitor);
            indexExpression.visit(visitor);
        }
        visitor.afterVariable(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return new OutputBuilderImpl()
                .add(arrayExpression.print(qualification))
                .add(SymbolEnum.LEFT_BRACKET)
                .add(indexExpression.print(qualification))
                .add(SymbolEnum.RIGHT_BRACKET);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        Stream<Variable> s1 = arrayExpression.variables(descendMode);
        Stream<Variable> s2 = indexExpression.variables(descendMode);
        return Stream.concat(Stream.of(this), Stream.concat(s1, s2));
    }

    @Override
    public Stream<TypeReference> typesReferenced() {
        return Stream.concat(arrayExpression.typesReferenced(), indexExpression.typesReferenced());
    }

    @Override
    public String fullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public String simpleName() {
        return simpleName;
    }

    @Override
    public Variable indexVariable() {
        return indexVariable;
    }

    @Override
    public Expression arrayExpression() {
        return arrayExpression;
    }

    @Override
    public Expression indexExpression() {
        return indexExpression;
    }

    @Override
    public boolean scopeIsRecursively(Variable variable) {
        if (variable.equals(arrayVariable)) return true;
        return arrayVariable.scopeIsRecursively(variable);
    }
}
