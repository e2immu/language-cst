package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.DetailedSources;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.type.ParameterizedType;

import java.util.*;

public class DetailedSourcesImpl implements DetailedSources {
    private final IdentityHashMap<Object, Object> identityHashMap;
    private final IdentityHashMap<Object, Object> association;

    private DetailedSourcesImpl(IdentityHashMap<Object, Object> identityHashMap,
                                IdentityHashMap<Object, Object> association) {
        this.identityHashMap = identityHashMap;
        this.association = association;
    }

    public static class BuilderImpl implements DetailedSources.Builder {
        private final IdentityHashMap<Object, Object> identityHashMap = new IdentityHashMap<>();
        private IdentityHashMap<Object, Object> association;

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
            return new DetailedSourcesImpl(identityHashMap, association);
        }

        @Override
        public Builder putAssociatedObject(ParameterizedType from, ParameterizedType to) {
            if (association == null) association = new IdentityHashMap<>();
            association.put(from, to);
            return this;
        }
    }

    @Override
    public Object associatedObject(Object object) {
        assert association != null;
        return association.get(object);
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
        IdentityHashMap<Object, Object> copyAssociation;
        IdentityHashMap<Object, Object> otherAssociation = ((DetailedSourcesImpl) other).association;
        if (this.association == null) {
            if (otherAssociation != null) {
                copyAssociation = new IdentityHashMap<>(otherAssociation);
            } else {
                copyAssociation = null;
            }
        } else {
            copyAssociation = new IdentityHashMap<>(this.association);
            if (otherAssociation != null) {
                copyAssociation.putAll(otherAssociation);
            }
        }
        return new DetailedSourcesImpl(copy, copyAssociation);
    }
}
