package org.e2immu.cstimpl.expression;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstimpl.expression.util.ExtractComponentsOfTooComplex;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ExpressionCanBeTooComplex extends ExpressionImpl {
    protected ExpressionCanBeTooComplex(int complexity) {
        super(complexity);
    }

    // make a MultiValue with one component per variable (so that they are marked "read")
    // and one per assignment. Even though the And may be too complex, we should not ignore READ/ASSIGNED AT
    // information
    public static Expression reducedComplexity(Runtime runtime, List<Expression> values) {
        ParameterizedType booleanType = runtime.booleanParameterizedType();

        Expression instance = runtime.newInstanceForTooComplex(booleanType);

        // IMPORTANT: instance has to be the last one, it determines type, delay, etc.
        Stream<Expression> components = values.stream().flatMap(e -> collect(runtime, e).stream());
        List<Expression> newExpressions = Stream.concat(components.distinct().sorted(), Stream.of(instance))
                .toList();
        return runtime.newMultiExpressions(newExpressions);
    }

    /*
    goal is to replicate the ContextNotNull environment as much as possible (anything that increases Context
    properties should be evaluated, for consistency's sake)
     */
    private static Set<Expression> collect(Runtime runtime, Expression expression) {
        ExtractComponentsOfTooComplex ev = new ExtractComponentsOfTooComplex(runtime);
        expression.visit(ev);
        return ev.getExpressions();
    }
}
