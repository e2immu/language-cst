package org.e2immu.language.cst.api.analysis;

import java.util.stream.Stream;

public interface PropertyValueMap {

    record PropertyValue(Property property, Value value) {
    }

    Property property(String key);

    <V extends Value> V getOrDefault(Property property, V defaultValue);

    boolean haveAnalyzedValueFor(Property property);

    default boolean haveAnalyzedValueFor(Property property, Runnable runWhenNoValue) {
        boolean b = haveAnalyzedValueFor(property);
        if (!b) runWhenNoValue.run();
        return b;
    }

    Stream<PropertyValue> propertyValueStream();

    <V extends Value> void set(Property property, V value);
}
