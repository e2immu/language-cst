package org.e2immu.cstimpl.type;

import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.type.TypeParameter;
import org.e2immu.cstimpl.output.*;
import org.e2immu.support.Either;
import org.e2immu.support.FirstThen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TypeParameterImpl implements TypeParameter {
    private final int index;
    private final String name;
    private final Either<TypeInfo, MethodInfo> owner;
    private final FirstThen<Builder, List<ParameterizedType>> typeBounds;

    public TypeParameterImpl(int index, String name, Either<TypeInfo, MethodInfo> owner) {
        this.index = index;
        this.name = name;
        this.owner = owner;
        this.typeBounds = new FirstThen<>(new Builder(this));
    }

    private void commit(List<ParameterizedType> typeBounds) {
        this.typeBounds.set(typeBounds);
    }

    @Override
    public TypeParameter.Builder builder() {
        return typeBounds.getFirst();
    }

    public static class Builder implements TypeParameter.Builder {
        private final TypeParameterImpl typeParameter;
        private List<ParameterizedType> typeBounds = new ArrayList<>();

        public Builder(TypeParameterImpl tpi) {
            this.typeParameter = tpi;
        }

        @Override
        public Builder setTypeBounds(List<ParameterizedType> typeBounds) {
            this.typeBounds = typeBounds;
            return this;
        }

        @Override
        public Builder addTypeBound(ParameterizedType typeBound) {
            typeBounds.add(typeBound);
            return this;
        }

        @Override
        public TypeParameter commit() {
            typeParameter.commit(List.copyOf(typeBounds));
            return typeParameter;
        }
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Either<TypeInfo, MethodInfo> getOwner() {
        return owner;
    }

    @Override
    public List<ParameterizedType> typeBounds() {
        return typeBounds.get();
    }

    @Override
    public ParameterizedType asParameterizedType(Runtime runtime) {
        // NOTE: we do not add the type bounds
        return new ParameterizedTypeImpl(this, 0);
    }

    @Override
    public ParameterizedType asSimpleParameterizedType() {
        return new ParameterizedTypeImpl(this, 0);
    }

    @Override
    public OutputBuilder print(Qualification qualification, Set<TypeParameter> visitedTypeParameters) {
        OutputBuilder outputBuilder = new OutputBuilderImpl().add(new TextImpl(name));
        if (typeBounds.isSet() && !typeBounds.get().isEmpty()
            && visitedTypeParameters != null && !visitedTypeParameters.contains(this)) {
            visitedTypeParameters.add(this);
            outputBuilder.add(SpaceEnum.ONE).add(KeywordImpl.EXTENDS).add(SpaceEnum.ONE);
            outputBuilder.add(typeBounds.get()
                    .stream()
                    .map(pt -> ParameterizedTypePrinter.print(qualification, pt, false,
                            DiamondEnum.SHOW_ALL, false, visitedTypeParameters))
                    .collect(OutputBuilderImpl.joining(SymbolEnum.AND_TYPES)));
        }
        return outputBuilder;
    }

    @Override
    public String simpleName() {
        return name;
    }
}
