package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.DetailedSources;
import org.e2immu.language.cst.api.element.Source;

import java.util.*;

public class DetailedSourcesImpl implements DetailedSources {
    private final IdentityHashMap<Object, Object> identityHashMap;

    private DetailedSourcesImpl(IdentityHashMap<Object, Object> identityHashMap) {
        this.identityHashMap = identityHashMap;
    }

    public static class BuilderImpl implements DetailedSources.Builder {
        private final IdentityHashMap<Object, Object> identityHashMap = new IdentityHashMap<>();

        @Override
        public Builder copy() {
            BuilderImpl copy = new BuilderImpl();
            copy.identityHashMap.putAll(identityHashMap);
            return copy;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Builder put(Object object, Source source) {
            Object current = identityHashMap.get(object);
            if (current == null) {
                identityHashMap.put(object, source);
            } else if (current instanceof List list) {
                list.add(source);
            } else if (current instanceof Source s) {
                List<Source> list = new ArrayList<>();
                list.add(s);
                list.add(source);
                identityHashMap.put(object, list);
            }
            return this;
        }

        @Override
        public DetailedSourcesImpl build() {
            return new DetailedSourcesImpl(identityHashMap);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Source> details(Object object) {
        return (List<Source>) identityHashMap.get(object);
    }

    public Source detail(Object object) {
        return (Source) identityHashMap.get(object);
    }

    @Override
    public DetailedSources merge(DetailedSources other) {
        IdentityHashMap<Object, Object> copy = new IdentityHashMap<>(this.identityHashMap);
        copy.putAll(((DetailedSourcesImpl) other).identityHashMap);
        return new DetailedSourcesImpl(copy);
    }
}
