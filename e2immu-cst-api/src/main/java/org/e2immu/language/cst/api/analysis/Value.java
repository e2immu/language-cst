package org.e2immu.language.cst.api.analysis;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.FieldInfo;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.info.ParameterInfo;
import org.e2immu.language.cst.api.util.ParSeq;

import java.util.Map;
import java.util.Set;

public interface Value extends Comparable<Value> {
    Codec.EncodedValue encode(Codec codec);

    @Override
    default int compareTo(Value o) {
        throw new UnsupportedOperationException();
    }

    default boolean lt(Value other) {
        return compareTo(other) < 0;
    }

    default Value min(Value v) {
        return v == null || compareTo(v) <= 0 ? this : v;
    }

    interface Bool extends Value {
        boolean isTrue();

        boolean isFalse();
    }

    interface Immutable extends Value {
        boolean isAtLeastImmutableHC();

        boolean isImmutable();

        Immutable max(Immutable other);

        Immutable min(Immutable other);
    }


    interface Independent extends Value {

        boolean isAtLeastIndependentHc();

        boolean isIndependent();
    }

    interface NotNull extends Value {

        boolean isNullable();
    }

    /*
    the strings are arbitrary labels.
    at least two methods should have the same label, of the same kind (seq, par, multi).
     */
    interface CommutableData extends Value {
        default boolean isDefault() {
            return par().isBlank() && seq().isBlank() && multi().isBlank();
        }

        default boolean isParallel() {
            return !par().isBlank() && seq().isBlank();
        }

        default boolean isSequential() {
            return par().isBlank() && !seq().isBlank();
        }

        String multi();

        String par();

        String seq();
    }

    // meant for the "GetSetField" property
    interface FieldValue extends Value {
        FieldInfo field();
    }

    interface FieldBooleanMap extends Value {
        Map<FieldInfo, Boolean> map();
    }

    // meant for the "GetSetEquivalent" property
    interface GetSetEquivalent extends Value {
        Set<ParameterInfo> parameters();

        MethodInfo methodWithoutParameters();
    }

    // meant for parallel parameter groups
    interface ParameterParSeq extends Value {
        ParSeq<ParameterInfo> parSeq();
    }

    interface PostConditions extends Value {
        Map<String, Expression> byIndex();
    }

    interface Precondition extends Value {
        Expression expression();
    }

    // for parameters

    interface AssignedToField extends Value {
        Set<FieldInfo> fields();
    }

    interface IndicesOfEscapes extends Value {
        Set<String> indices();
    }
}
