package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.DetailedSources;
import org.e2immu.language.cst.api.element.Source;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class DetailedSourcesImpl implements DetailedSources {
    private final IdentityHashMap<Object, Source> identityHashMap;
    private final Map<String, DetailedSources> layers;

    private DetailedSourcesImpl(IdentityHashMap<Object, Source> identityHashMap,
                                Map<String, DetailedSources> layers) {
        this.identityHashMap = identityHashMap;
        this.layers = layers;
    }

    public static class BuilderImpl implements DetailedSources.Builder {
        private IdentityHashMap<Object, Source> identityHashMap;
        private Map<String, DetailedSources> layers;

        @Override
        public Builder put(Object object, Source source) {
            if (identityHashMap == null) identityHashMap = new IdentityHashMap<>();
            identityHashMap.put(object, source);
            return this;
        }

        @Override
        public Builder addLayer(String key, DetailedSources detailedSources) {
            if (layers == null) layers = new HashMap<>();
            layers.put(key, detailedSources);
            return this;
        }

        @Override
        public DetailedSourcesImpl build() {
            return new DetailedSourcesImpl(identityHashMap, layers);
        }
    }

    @Override
    public DetailedSources layer(String layer) {
        return layers == null ? null : layers.get(layer);
    }

    public Source detail(Object object) {
        return identityHashMap.get(object);
    }
}
