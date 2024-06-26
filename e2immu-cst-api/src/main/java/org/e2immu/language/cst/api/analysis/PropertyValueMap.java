package org.e2immu.language.cst.api.analysis;

public interface PropertyValueMap {

    Property property(String key);

    <V extends Value> V getOrDefault(Property property, V defaultValue);

    boolean haveAnalyzedValueFor(Property property);

    <V extends Value> void set(Property property, V value);
}
