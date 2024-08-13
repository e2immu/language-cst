package org.e2immu.language.cst.impl.translate;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.MethodCall;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.FieldReference;
import org.e2immu.language.cst.api.variable.LocalVariable;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.expression.VariableExpressionImpl;
import org.e2immu.language.cst.impl.type.ParameterizedTypeImpl;
import org.e2immu.language.cst.impl.variable.FieldReferenceImpl;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TranslationMapImpl implements TranslationMap {

    private final Map<? extends Variable, ? extends Variable> variables;
    private final Map<MethodInfo, List<MethodInfo>> methods;
    private final Map<? extends Expression, ? extends Expression> expressions;
    private final Map<? extends Statement, List<Statement>> statements;
    private final Map<ParameterizedType, ParameterizedType> types;
    private final Map<LocalVariable, LocalVariable> localVariables;
    private final Map<? extends Variable, ? extends Expression> variableExpressions;
    private final boolean expandDelayedWrappedExpressions;
    private final boolean recurseIntoScopeVariables;
    private final boolean yieldIntoReturn;
    private final boolean translateAgain;
    private final ModificationTimesHandler modificationTimesHandler;

    private TranslationMapImpl(Map<? extends Statement, List<Statement>> statements,
                               Map<? extends Expression, ? extends Expression> expressions,
                               Map<? extends Variable, ? extends Expression> variableExpressions,
                               Map<? extends Variable, ? extends Variable> variables,
                               Map<MethodInfo, List<MethodInfo>> methods,
                               Map<ParameterizedType, ParameterizedType> types,
                               ModificationTimesHandler modificationTimesHandler,
                               boolean expandDelayedWrappedExpressions,
                               boolean recurseIntoScopeVariables,
                               boolean yieldIntoReturn,
                               boolean translateAgain) {
        this.variables = variables;
        this.expressions = expressions;
        this.variableExpressions = variableExpressions;
        this.statements = statements;
        this.methods = methods;
        this.types = types;//checkForCycles(types);
        this.yieldIntoReturn = yieldIntoReturn;
        localVariables = variables.entrySet().stream()
                .filter(e -> e.getKey() instanceof LocalVariable && e.getValue() instanceof LocalVariable)
                .collect(Collectors.toMap(e -> ((LocalVariable) e.getKey()), e -> ((LocalVariable) e.getValue())));
        this.expandDelayedWrappedExpressions = expandDelayedWrappedExpressions;
        this.recurseIntoScopeVariables = recurseIntoScopeVariables;
        this.translateAgain = translateAgain;
        this.modificationTimesHandler = modificationTimesHandler;
    }

    public static class Builder implements TranslationMap.Builder {

        private final Map<Variable, Variable> variables = new HashMap<>();
        private final Map<Expression, Expression> expressions = new HashMap<>();
        private final Map<Variable, Expression> variableExpressions = new HashMap<>();
        private final Map<MethodInfo, List<MethodInfo>> methods = new HashMap<>();
        private final Map<Statement, List<Statement>> statements = new HashMap<>();
        private final Map<ParameterizedType, ParameterizedType> types = new HashMap<>();
        private ModificationTimesHandler modificationTimesHandler;
        private boolean expandDelayedWrappedExpressions;
        private boolean recurseIntoScopeVariables;
        private boolean yieldIntoReturn;
        private boolean translateAgain;

        public Builder() {
        }

        public Builder(TranslationMap other) {
            variables.putAll(other.variables());
            expressions.putAll(other.expressions());
            variableExpressions.putAll(other.variableExpressions());
            methods.putAll(other.methods());
            statements.putAll(other.statements());
            types.putAll(other.types());
            expandDelayedWrappedExpressions = other.expandDelayedWrappedExpressions();
            recurseIntoScopeVariables = other.recurseIntoScopeVariables();
            yieldIntoReturn = other.translateYieldIntoReturn();
            translateAgain = other.translateAgain();
        }

        @Override
        public TranslationMap build() {
            return new TranslationMapImpl(statements, expressions, variableExpressions, variables, methods, types,
                    modificationTimesHandler,
                    expandDelayedWrappedExpressions, recurseIntoScopeVariables, yieldIntoReturn, translateAgain);
        }

        @Override
        public Builder setTranslateAgain(boolean translateAgain) {
            this.translateAgain = translateAgain;
            return this;
        }

        @Override
        public Builder setRecurseIntoScopeVariables(boolean recurseIntoScopeVariables) {
            this.recurseIntoScopeVariables = recurseIntoScopeVariables;
            return this;
        }

        @Override
        public Builder put(Statement template, Statement actual) {
            statements.put(template, List.of(actual));
            return this;
        }

        @Override
        public Builder put(MethodInfo template, List<MethodInfo> actual) {
            methods.put(template, actual);
            return this;
        }

        @Override
        public Builder put(Statement template, List<Statement> statements) {
            this.statements.put(template, statements);
            return this;
        }

        @Override
        public Builder put(Expression template, Expression actual) {
            this.expressions.put(template, actual);
            return this;
        }

        @Override
        public Builder addVariableExpression(Variable variable, Expression actual) {
            variableExpressions.put(variable, actual);
            return this;
        }

        @Override
        public Builder renameVariable(Variable variable, Expression actual) {
            variableExpressions.put(variable, actual);
            return this;
        }

        @Override
        public Builder put(ParameterizedType template, ParameterizedType actual) {
            types.put(template, actual);
            return this;
        }

        @Override
        public TranslationMap.Builder put(Variable template, Variable actual) {
            variables.put(template, actual);
            return this;
        }

        @Override
        public Builder setYieldToReturn(boolean b) {
            this.yieldIntoReturn = b;
            return this;
        }

        @Override
        public Builder setExpandDelayedWrapperExpressions(boolean expandDelayedWrappedExpressions) {
            this.expandDelayedWrappedExpressions = expandDelayedWrappedExpressions;
            return this;
        }

        @Override
        public boolean translateMethod(MethodInfo methodInfo) {
            return methods.containsKey(methodInfo);
        }

        @Override
        public Builder setModificationTimesHandler(ModificationTimesHandler modificationTimesHandler) {
            this.modificationTimesHandler = modificationTimesHandler;
            return this;
        }

        @Override
        public boolean isEmpty() {
            return statements.isEmpty()
                   && expressions.isEmpty()
                   && variables.isEmpty()
                   && methods.isEmpty()
                   && types.isEmpty()
                   && variableExpressions.isEmpty();
        }
    }

    @Override
    public boolean expandDelayedWrappedExpressions() {
        return expandDelayedWrappedExpressions;
    }

    @Override
    public String toString() {
        return "TM{" + variables.size() + "," + methods.size() + "," + expressions.size() + "," + statements.size()
               + "," + types.size() + "," + localVariables.size() + "," + variableExpressions.size() +
               (expandDelayedWrappedExpressions ? ",expand" : "") + "}";
    }

    @Override
    public boolean translateYieldIntoReturn() {
        return yieldIntoReturn;
    }

    @Override
    public boolean hasVariableTranslations() {
        return !variables.isEmpty();
    }

    @Override
    public boolean recurseIntoScopeVariables() {
        return recurseIntoScopeVariables;
    }

    @Override
    public Expression translateExpression(Expression expression) {
        return Objects.requireNonNullElse(expressions.get(expression), expression);
    }

    @Override
    public List<MethodInfo> translateMethod(MethodInfo methodInfo) {
        return methods.getOrDefault(methodInfo, List.of(methodInfo));
    }

    @Override
    public Variable translateVariable(Variable variable) {
        Variable v = variables.get(variable);
        if (v != null) return v;
        if (variable instanceof FieldReference fr && fr.scopeVariable() != null) {
            Variable scopeTranslated = translateVariable(fr.scopeVariable());
            if (scopeTranslated != fr.scopeVariable()) {
                Expression e = new VariableExpressionImpl(fr.source(), fr.comments(), scopeTranslated, null);
                return new FieldReferenceImpl(fr.fieldInfo(), e, scopeTranslated, fr.fieldInfo().type());
            }
        }
        if (variable instanceof LocalVariable lv && lv.assignmentExpression() != null) {
            Expression te = lv.assignmentExpression().translate(this);
            if(te != lv.assignmentExpression()) {
                return lv.withAssignmentExpression(te);
            }
        }
        return variable;
    }

    @Override
    public Expression translateVariableExpressionNullIfNotTranslated(Variable variable) {
        return variableExpressions.get(variable);
    }

    @Override
    public List<Statement> translateStatement(Statement statement) {
        List<Statement> list = statements.get(statement);
        return list == null ? List.of(statement) : list;
    }

    @Override
    public ParameterizedType translateType(ParameterizedType parameterizedType) {
        ParameterizedType inMap = types.get(parameterizedType);
        if (inMap != null) return inMap;
        List<ParameterizedType> params = parameterizedType.parameters();
        List<ParameterizedType> translatedTypes = params.isEmpty() ? params :
                params.stream().map(this::translateType).collect(toList(params));
        if (params == translatedTypes) return parameterizedType;
        return new ParameterizedTypeImpl(parameterizedType.typeInfo(), null, translatedTypes,
                parameterizedType.arrays(), parameterizedType.wildcard());
    }

    @Override
    public boolean isEmpty() {
        return statements.isEmpty() && expressions.isEmpty() && methods.isEmpty() &&
               types.isEmpty() && variables.isEmpty() && localVariables.isEmpty() && variableExpressions.isEmpty();
    }

    @Override
    public Map<? extends Variable, ? extends Variable> variables() {
        return variables;
    }

    @Override
    public Map<? extends Expression, ? extends Expression> expressions() {
        return expressions;
    }

    @Override
    public Map<? extends Variable, ? extends Expression> variableExpressions() {
        return variableExpressions;
    }

    @Override
    public Map<MethodInfo, List<MethodInfo>> methods() {
        return methods;
    }

    @Override
    public Map<ParameterizedType, ParameterizedType> types() {
        return types;
    }

    @Override
    public Map<? extends Statement, List<Statement>> statements() {
        return statements;
    }

    @Override
    public boolean translateAgain() {
        return translateAgain;
    }

    @Override
    public String modificationTimes(Expression methodCallBeforeTranslation,
                                    Expression translatedObject, List<Expression> translatedParameters) {
        if (modificationTimesHandler == null) return null;
        // type cast: see interface spec: methodCallBeforeTranslation is of type Expression to avoid cyclic type dependencies
        MethodCall beforeTranslation;
        if ((beforeTranslation = methodCallBeforeTranslation.asInstanceOf(MethodCall.class)) != null) {
            return modificationTimesHandler.modificationTimes(beforeTranslation, translatedObject, translatedParameters);
        }
        throw new UnsupportedOperationException();
    }
}
