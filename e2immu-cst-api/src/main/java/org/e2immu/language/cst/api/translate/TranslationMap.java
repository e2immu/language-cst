package org.e2immu.language.cst.api.translate;

import org.e2immu.annotation.Fluent;
import org.e2immu.annotation.NotNull;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.expression.MethodCall;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;

public interface TranslationMap {

    @NotNull
    default Expression translateExpression(Expression expression) {
        return expression;
    }

    @NotNull
    default MethodInfo translateMethod(MethodInfo methodInfo) {
        return methodInfo;
    }

    @NotNull
    default Variable translateVariable(Variable variable) {
        return variable;
    }

    @NotNull(content = true)
    default List<Statement> translateStatement(Statement statement) {
        return List.of(statement);
    }

    @NotNull
    default ParameterizedType translateType(ParameterizedType parameterizedType) {
        return parameterizedType;
    }

    default boolean isEmpty() {
        return true;
    }

    /*
     because equality of delayed variables is based on ==
     */
    default Expression translateVariableExpressionNullIfNotTranslated(Variable variable) {
        return null;
    }

    default boolean hasVariableTranslations() {
        return false;
    }

    /*
     unlike in merge, in the case of ExplicitConstructorInvocation, we cannot predict which fields need their scope translating
     */
    default boolean recurseIntoScopeVariables() {
        return false;
    }

    default boolean expandDelayedWrappedExpressions() {
        return false;
    }

    default boolean translateYieldIntoReturn() {
        return false;
    }

    default Map<? extends Variable, ? extends Variable> variables() {
        return Map.of();
    }

    default Map<? extends Expression, ? extends Expression> expressions() {
        return Map.of();
    }

    default Map<? extends Variable, ? extends Expression> variableExpressions() {
        return Map.of();
    }

    default Map<MethodInfo, MethodInfo> methods() {
        return Map.of();
    }

    default Map<? extends Statement, List<Statement>> statements() {
        return Map.of();
    }

    default Map<ParameterizedType, ParameterizedType> types() {
        return Map.of();
    }

    /*
    used by CM
     */
    default boolean translateAgain() {
        return false;
    }

    interface ModificationTimesHandler {
        String modificationTimes(MethodCall beforeTranslation,
                                 Expression translatedObject, List<Expression> translatedParameters);
    }
    /*
    Note: to avoid cyclic type dependencies, the first parameter takes 'Expression' rather than 'MethodCall'
     */
    default String modificationTimes(Expression methodCallBeforeTranslation,
                                     Expression translatedObject,
                                     List<Expression> translatedParameters) {
        return null;
    }

    default <T> Collector<T, List<T>, List<T>> toList(List<T> original) {
        throw new UnsupportedOperationException();
    }

    default <T> Collector<T, Set<T>, Set<T>> toSet(Set<T> original) {
        throw new UnsupportedOperationException();
    }

    default <K, V> Collector<Map.Entry<K, V>, Map<K, V>, Map<K, V>> toMap(Map<K, V> original) {
        throw new UnsupportedOperationException();
    }

    interface Builder {
        TranslationMap build();

        @Fluent
        Builder setTranslateAgain(boolean translateAgain);

        Builder setRecurseIntoScopeVariables(boolean recurseIntoScopeVariables);

        Builder put(Statement template, Statement actual);

        Builder put(MethodInfo template, MethodInfo actual);

        Builder put(Statement template, List<Statement> statements);

        Builder put(Expression template, Expression actual);

        Builder addVariableExpression(Variable variable, Expression actual);

        Builder renameVariable(Variable variable, Expression actual);

        Builder put(ParameterizedType template, ParameterizedType actual);

        Builder put(Variable template, Variable actual);

        Builder setYieldToReturn(boolean b);

        Builder setExpandDelayedWrapperExpressions(boolean expandDelayedWrappedExpressions);

        boolean translateMethod(MethodInfo methodInfo);

        Builder setModificationTimesHandler(ModificationTimesHandler modificationTimesHandler);

        boolean isEmpty();
    }
}
