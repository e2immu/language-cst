package org.e2immu.language.cst.impl.analysis;

import org.e2immu.language.cst.api.analysis.Property;
import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.analysis.Value;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.support.SetOnceMap;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class PropertyValueMapImpl implements PropertyValueMap {
    private final SetOnceMap<Property, Value> map = new SetOnceMap<>();

    @Override
    public PropertyValueMap rewire(InfoMap infoMap) {
        PropertyValueMapImpl rewiredMap = new PropertyValueMapImpl();
        map.stream().forEach(e -> rewiredMap.set(e.getKey(), e.getValue().rewire(infoMap)));
        return rewiredMap;
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

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Value> V getOrCreate(Property property, Supplier<V> createDefaultValue) {
        V v = (V) map.getOrDefaultNull(property);
        if (v != null) return v;
        V vv = createDefaultValue.get();
        map.put(property, vv);
        return vv;
    }

    @Override
    public void set(Property property, Value value) {
        assert property.classOfValue().isAssignableFrom(value.getClass());
        map.put(property, value);
    }

    @Override
    public void setAll(PropertyValueMap analysis) {
        analysis.propertyValueStream().forEach(pv -> map.put(pv.property(), pv.value()));
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
}
