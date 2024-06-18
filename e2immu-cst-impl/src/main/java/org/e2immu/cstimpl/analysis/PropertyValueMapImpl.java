package org.e2immu.cstimpl.analysis;

import org.e2immu.cstapi.analysis.Property;
import org.e2immu.cstapi.analysis.PropertyValueMap;
import org.e2immu.cstapi.analysis.Value;
import org.e2immu.support.SetOnceMap;

import java.util.HashMap;
import java.util.Map;

import static org.e2immu.cstimpl.analysis.PropertyImpl.*;

public class PropertyValueMapImpl implements PropertyValueMap {
    private final SetOnceMap<Property, Value> map = new SetOnceMap<>();

    private static final Map<String, Property> propertyMap = new HashMap<>();

    static {
        propertyMap.put(ALWAYS_ESCAPES.key(), ALWAYS_ESCAPES);
        propertyMap.put(COMMUTABLE_METHODS.key(), COMMUTABLE_METHODS);
        propertyMap.put(FINAL_FIELD.key(), FINAL_FIELD);
        propertyMap.put(FLUENT_METHOD.key(), FLUENT_METHOD);
        propertyMap.put(GET_SET_EQUIVALENT.key(), GET_SET_EQUIVALENT);
        propertyMap.put(GET_SET_FIELD.key(), GET_SET_FIELD);
        propertyMap.put(IDENTITY_METHOD.key(), IDENTITY_METHOD);
        propertyMap.put(IGNORE_MODIFICATIONS_FIELD.key(), IGNORE_MODIFICATIONS_FIELD);
        propertyMap.put(IGNORE_MODIFICATIONS_PARAMETER.key(), IGNORE_MODIFICATIONS_PARAMETER);
        propertyMap.put(IMMUTABLE_TYPE.key(), IMMUTABLE_TYPE);
        propertyMap.put(INDICES_OF_ESCAPE_METHOD.key(), INDICES_OF_ESCAPE_METHOD);
        propertyMap.put(NOT_NULL_FIELD.key(), NOT_NULL_FIELD);
        propertyMap.put(NOT_NULL_METHOD.key(), NOT_NULL_METHOD);
        propertyMap.put(METHOD_ALLOWS_INTERRUPTS.key(), METHOD_ALLOWS_INTERRUPTS);
        propertyMap.put(MODIFIED_METHOD.key(), MODIFIED_METHOD);
        propertyMap.put(MODIFIED_PARAMETER.key(), MODIFIED_PARAMETER);
        propertyMap.put(OWN_FIELDS_READ_MODIFIED_IN_METHOD.key(), OWN_FIELDS_READ_MODIFIED_IN_METHOD);
        propertyMap.put(PARALLEL_PARAMETER_GROUPS.key(), PARALLEL_PARAMETER_GROUPS);
        propertyMap.put(PARAMETER_ASSIGNED_TO_FIELD.key(), PARAMETER_ASSIGNED_TO_FIELD);
        propertyMap.put(POST_CONDITIONS_METHOD.key(), POST_CONDITIONS_METHOD);
        propertyMap.put(PRECONDITION_METHOD.key(), PRECONDITION_METHOD);
        propertyMap.put(SHALLOW_ANALYZER.key(), SHALLOW_ANALYZER);
        propertyMap.put(STATIC_SIDE_EFFECTS_METHOD.key(), STATIC_SIDE_EFFECTS_METHOD);
    }

    @Override
    public Property property(String key) {
        Property property = propertyMap.get(key);
        if (property == null) throw new UnsupportedOperationException("Unknown property " + key);
        return property;
    }

    @Override
    public boolean haveAnalyzedValueFor(Property property) {
        return map.isSet(property);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Value> V getOrDefault(Property property, V defaultValue) {
        assert defaultValue != null;
        return (V) map.getOrDefault(property, defaultValue);
    }

    @Override
    public void set(Property property, Value value) {
        assert property.classOfValue().isAssignableFrom(value.getClass());
        map.put(property, value);
    }

}
