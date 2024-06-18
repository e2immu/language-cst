package org.e2immu.cstapi.analysis;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.info.FieldInfo;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.ParameterInfo;
import org.e2immu.cstapi.util.ParSeq;

import java.util.Map;
import java.util.Set;

public interface Value {
    Codec.EncodedValue encode(Codec codec);

    interface Bool extends Value {
        boolean isTrue();

        boolean isFalse();
    }

    interface Immutable extends Value {
        boolean isAtLeastImmutableHC();
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
