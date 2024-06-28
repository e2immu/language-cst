package org.e2immu.language.cst.impl.analysis;

import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.support.SetOnceMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PropertyValueMapImpl implements PropertyValueMap {
    private final SetOnceMap<Property, Value> map = new SetOnceMap<>();

    private static final Map<String, Property> propertyMap = new HashMap<>();

    static {
        propertyMap.put(PropertyImpl.ALWAYS_ESCAPES.key(), PropertyImpl.ALWAYS_ESCAPES);
        propertyMap.put(PropertyImpl.COMMUTABLE_METHODS.key(), PropertyImpl.COMMUTABLE_METHODS);
        propertyMap.put(PropertyImpl.FINAL_FIELD.key(), PropertyImpl.FINAL_FIELD);
        propertyMap.put(PropertyImpl.FLUENT_METHOD.key(), PropertyImpl.FLUENT_METHOD);
        propertyMap.put(PropertyImpl.GET_SET_EQUIVALENT.key(), PropertyImpl.GET_SET_EQUIVALENT);
        propertyMap.put(PropertyImpl.GET_SET_FIELD.key(), PropertyImpl.GET_SET_FIELD);
        propertyMap.put(PropertyImpl.IDENTITY_METHOD.key(), PropertyImpl.IDENTITY_METHOD);
        propertyMap.put(PropertyImpl.IGNORE_MODIFICATIONS_FIELD.key(), PropertyImpl.IGNORE_MODIFICATIONS_FIELD);
        propertyMap.put(PropertyImpl.IGNORE_MODIFICATIONS_PARAMETER.key(), PropertyImpl.IGNORE_MODIFICATIONS_PARAMETER);
        propertyMap.put(PropertyImpl.IMMUTABLE_TYPE.key(), PropertyImpl.IMMUTABLE_TYPE);
        propertyMap.put(PropertyImpl.INDICES_OF_ESCAPE_METHOD.key(), PropertyImpl.INDICES_OF_ESCAPE_METHOD);
        propertyMap.put(PropertyImpl.NOT_NULL_FIELD.key(), PropertyImpl.NOT_NULL_FIELD);
        propertyMap.put(PropertyImpl.NOT_NULL_METHOD.key(), PropertyImpl.NOT_NULL_METHOD);
        propertyMap.put(PropertyImpl.METHOD_ALLOWS_INTERRUPTS.key(), PropertyImpl.METHOD_ALLOWS_INTERRUPTS);
        propertyMap.put(PropertyImpl.MODIFIED_METHOD.key(), PropertyImpl.MODIFIED_METHOD);
        propertyMap.put(PropertyImpl.MODIFIED_PARAMETER.key(), PropertyImpl.MODIFIED_PARAMETER);
        propertyMap.put(PropertyImpl.OWN_FIELDS_READ_MODIFIED_IN_METHOD.key(), PropertyImpl.OWN_FIELDS_READ_MODIFIED_IN_METHOD);
        propertyMap.put(PropertyImpl.PARALLEL_PARAMETER_GROUPS.key(), PropertyImpl.PARALLEL_PARAMETER_GROUPS);
        propertyMap.put(PropertyImpl.PARAMETER_ASSIGNED_TO_FIELD.key(), PropertyImpl.PARAMETER_ASSIGNED_TO_FIELD);
        propertyMap.put(PropertyImpl.POST_CONDITIONS_METHOD.key(), PropertyImpl.POST_CONDITIONS_METHOD);
        propertyMap.put(PropertyImpl.PRECONDITION_METHOD.key(), PropertyImpl.PRECONDITION_METHOD);
        propertyMap.put(PropertyImpl.SHALLOW_ANALYZER.key(), PropertyImpl.SHALLOW_ANALYZER);
        propertyMap.put(PropertyImpl.STATIC_SIDE_EFFECTS_METHOD.key(), PropertyImpl.STATIC_SIDE_EFFECTS_METHOD);
    }

    @Override
    public Property property(String key) {
        Property property = propertyMap.get(key);
        if (property == null) throw new UnsupportedOperationException("Unknown property " + key);
        return property;
    }

    @Override
    public Stream<PropertyValue> propertyValueStream() {
        return map.stream().map(e -> new PropertyValue(e.getKey(), e.getValue()));
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
