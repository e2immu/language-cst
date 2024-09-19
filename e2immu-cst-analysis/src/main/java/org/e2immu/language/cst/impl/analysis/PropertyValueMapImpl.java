package org.e2immu.language.cst.impl.analysis;

import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.support.SetOnceMap;

import java.util.*;
import java.util.stream.Stream;

import static org.e2immu.language.cst.impl.analysis.PropertyImpl.*;

public class PropertyValueMapImpl implements PropertyValueMap {
    private final SetOnceMap<Property, Value> map = new SetOnceMap<>();

    private static final List<Property> properties = new ArrayList<>();
    private static final Map<String, Property> propertyMap = new HashMap<>();

    static {
        Collections.addAll(properties,
                ALWAYS_ESCAPES,
                COMMUTABLE_METHODS,
                CONTAINER_FIELD,
                CONTAINER_METHOD,
                CONTAINER_PARAMETER,
                CONTAINER_TYPE,
                FINAL_FIELD,
                FLUENT_METHOD,
                GET_SET_EQUIVALENT,
                GET_SET_FIELD,
                IDENTITY_METHOD,
                IGNORE_MODIFICATIONS_FIELD,
                IGNORE_MODIFICATIONS_PARAMETER,
                IMMUTABLE_FIELD,
                IMMUTABLE_METHOD,
                IMMUTABLE_PARAMETER,
                IMMUTABLE_TYPE,
                IMMUTABLE_TYPE_DETERMINED_BY_PARAMETERS,
                INDEPENDENT_FIELD,
                INDEPENDENT_METHOD,
                INDEPENDENT_PARAMETER,
                INDEPENDENT_TYPE,
                INDICES_OF_ESCAPE_METHOD);
        Collections.addAll(properties,
                METHOD_ALLOWS_INTERRUPTS,
                MODIFIED_METHOD,
                MODIFIED_FIELD,
                MODIFIED_PARAMETER,
                NOT_NULL_FIELD,
                NOT_NULL_METHOD,
                NOT_NULL_PARAMETER,
                OWN_FIELDS_READ_MODIFIED_IN_METHOD,
                PARALLEL_PARAMETER_GROUPS,
                PARAMETER_ASSIGNED_TO_FIELD,
                POST_CONDITIONS_METHOD,
                PRECONDITION_METHOD,
                SHALLOW_ANALYZER,
                STATIC_SIDE_EFFECTS_METHOD
        );
        properties.forEach(p -> propertyMap.put(p.key(), p));
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

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Value> V getOrNull(Property property, Class<? extends V> clazz) {
        return (V) map.getOrDefaultNull(property);
    }

    @Override
    public void set(Property property, Value value) {
        assert property.classOfValue().isAssignableFrom(value.getClass());
        map.put(property, value);
    }

    @Override
    public void setAll(PropertyValueMap analysis) {
        propertyValueStream().forEach(pv -> map.put(pv.property(), pv.value()));
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
}
