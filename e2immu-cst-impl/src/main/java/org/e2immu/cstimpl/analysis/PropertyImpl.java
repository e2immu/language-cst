package org.e2immu.cstimpl.analysis;

import org.e2immu.cstapi.analysis.Property;
import org.e2immu.cstapi.analysis.Value;

public class PropertyImpl implements Property {
    // type
    public static final Property IMMUTABLE_TYPE = new PropertyImpl("immutableType", ValueImpl.ImmutableImpl.MUTABLE);

    // method
    public static final Property MODIFIED_METHOD = new PropertyImpl("modifiedMethod");
    public static final Property FLUENT_METHOD = new PropertyImpl("fluentMethod");
    public static final Property IDENTITY_METHOD = new PropertyImpl("identityMethod");
    public static final Property NOT_NULL_METHOD = new PropertyImpl("notNullMethod");
    public static final Property STATIC_SIDE_EFFECTS_METHOD = new PropertyImpl("sseMethod");
    public static final Property POST_CONDITIONS_METHOD = new PropertyImpl("postConditionsMethod",
            ValueImpl.PostConditionsImpl.EMPTY);
    public static final Property PRECONDITION_METHOD = new PropertyImpl("preconditionMethod",
            ValueImpl.PreconditionImpl.EMPTY);
    public static final Property INDICES_OF_ESCAPE_METHOD = new PropertyImpl("indicesOfEscapesNotInPrePostCondition",
            ValueImpl.IndicesOfEscapesImpl.EMPTY);
    public static final Property METHOD_ALLOWS_INTERRUPTS = new PropertyImpl("methodAllowsInterrupts");
    public static final Property OWN_FIELDS_READ_MODIFIED_IN_METHOD = new PropertyImpl("areOwnFieldsReadModified",
            ValueImpl.FieldBooleanMapImpl.EMPTY);

    // commutation on methods
    public static final Property PARALLEL_PARAMETER_GROUPS = new PropertyImpl("parallelParameterGroups",
            ValueImpl.ParameterParSeqImpl.EMPTY);
    public static final Property COMMUTABLE_METHODS = new PropertyImpl("commutableMethods",
            ValueImpl.CommutableDataImpl.BLANK);
    public static final Property GET_SET_FIELD = new PropertyImpl("getSetField",
            ValueImpl.FieldValueImpl.EMPTY);
    public static final Property GET_SET_EQUIVALENT = new PropertyImpl("getSetEquivalent",
            ValueImpl.GetSetEquivalentImpl.EMPTY);

    // parameter
    public static final Property MODIFIED_PARAMETER = new PropertyImpl("modifiedParameter");
    public static final Property IGNORE_MODIFICATIONS_PARAMETER = new PropertyImpl("ignoreModsParameter");
    public static final Property PARAMETER_ASSIGNED_TO_FIELD = new PropertyImpl("parameterAssignedToField",
            ValueImpl.AssignedToFieldImpl.EMPTY);

    // field
    public static final Property FINAL_FIELD = new PropertyImpl("finalField");
    public static final Property NOT_NULL_FIELD = new PropertyImpl("notNullField");
    public static final Property IGNORE_MODIFICATIONS_FIELD = new PropertyImpl("ignoreModificationsField");

    // statement
    public static final Property ALWAYS_ESCAPES = new PropertyImpl("statementAlwaysEscapes");

    // any element
    public static final Property SHALLOW_ANALYZER = new PropertyImpl("shallowAnalyzer");


    private final String key;
    private final Value defaultValue;

    public PropertyImpl(String key) {
        this(key, ValueImpl.BoolImpl.FALSE);
    }

    public PropertyImpl(String key, Value defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public Value defaultValue() {
        return defaultValue;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Class<? extends Value> classOfValue() {
        return defaultValue.getClass();
    }
}
