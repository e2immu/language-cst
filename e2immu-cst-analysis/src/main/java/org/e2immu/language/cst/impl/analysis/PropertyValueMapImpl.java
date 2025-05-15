package org.e2immu.language.cst.impl.analysis;

import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.info.InfoMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/*
Does not use SetOnceMap<> because of controlled overwrites.
 */
public class PropertyValueMapImpl implements PropertyValueMap {
    private final Map<Property, Value> map = new HashMap<>();

    @Override
    public PropertyValueMap rewire(InfoMap infoMap) {
        PropertyValueMapImpl rewiredMap = new PropertyValueMapImpl();
        map.forEach((key, value) -> rewiredMap.set(key, value.rewire(infoMap)));
        return rewiredMap;
    }

    @Override
    public Stream<PropertyValue> propertyValueStream() {
        return map.entrySet().stream().map(e -> new PropertyValue(e.getKey(), e.getValue()));
    }

    @Override
    public boolean haveAnalyzedValueFor(Property property) {
        return map.containsKey(property);
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
        return (V) map.get(property);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Value> V getOrCreate(Property property, Supplier<V> createDefaultValue) {
        V v = (V) map.get(property);
        if (v != null) return v;
        V vv = createDefaultValue.get();
        map.put(property, vv);
        return vv;
    }

    @Override
    public void set(Property property, Value value) {
        assert property.classOfValue().isAssignableFrom(value.getClass());
        if (map.put(property, value) != null) {
            throw new IllegalArgumentException("Trying to overwrite a value for property " + property);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Value> void setAllowControlledOverwrite(Property property, V value) {
        V current = (V) map.get(property);
        if (current == null || current.overwriteAllowed(value)) map.put(property, value);
    }

    @Override
    public void setAll(PropertyValueMap analysis) {
        analysis.propertyValueStream().forEach(pv -> set(pv.property(), pv.value()));
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
}
