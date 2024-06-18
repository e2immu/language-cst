package org.e2immu.cstapi.analysis;

public interface Property {
    Class<? extends Value> classOfValue();

    Value defaultValue();

    String key();
}
