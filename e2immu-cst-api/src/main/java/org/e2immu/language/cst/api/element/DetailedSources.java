package org.e2immu.language.cst.api.element;

public interface DetailedSources {
    DetailedSources layer(String layer);

    Source detail(Object object);

    interface Builder {

        Builder put(Object object, Source source);

        Builder addLayer(String key, DetailedSources detailedSources);

        DetailedSources build();
    }
}
