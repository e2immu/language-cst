package org.e2immu.language.cst.impl.type;

import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.type.TypeParameter;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.support.Either;
import org.e2immu.support.FirstThen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TypeParameterImpl implements TypeParameter {
    private final int index;
    private final String name;
    private final Either<TypeInfo, MethodInfo> owner;
    private final FirstThen<Builder, List<ParameterizedType>> typeBounds;
    private final List<AnnotationExpression> annotations;

    public TypeParameterImpl(int index, String name,
                             Either<TypeInfo, MethodInfo> owner,
                             List<AnnotationExpression> annotations) {
        this.index = index;
        this.name = name;
        this.owner = owner;
        this.typeBounds = new FirstThen<>(new Builder(this));
        this.annotations = annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeParameterImpl that)) return false;
        return getIndex() == that.getIndex() && Objects.equals(getOwner(), that.getOwner());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), getOwner());
    }

    private void commit(List<ParameterizedType> typeBounds) {
        this.typeBounds.set(typeBounds);
    }

    @Override
    public TypeParameter rewire(InfoMap infoMap) {
        Either<TypeInfo, MethodInfo> rewiredOwner = owner.isLeft()
                ? Either.left(infoMap.typeInfo(owner.getLeft()))
                : Either.right(infoMap.methodInfo(owner.getRight()));
        TypeParameter rewired = new TypeParameterImpl(index, name, rewiredOwner, annotations);
        typeBounds.get().forEach(pt -> rewired.builder().addTypeBound(pt.rewire(infoMap)));
        rewired.builder().commit();
        return rewired;
    }

    @Override
    public TypeParameter withOwnerVariableTypeBounds(MethodInfo methodInfo) {
        return withOwner(Either.right(methodInfo));
    }

    @Override
    public TypeParameter withOwnerVariableTypeBounds(TypeInfo typeInfo) {
        return withOwner(Either.left(typeInfo));
    }

    private TypeParameter withOwner(Either<TypeInfo, MethodInfo> owner) {
        TypeParameterImpl tpi = new TypeParameterImpl(getIndex(), simpleName(), owner, annotations());
        List<ParameterizedType> newBounds = typeBounds().stream()
                .map(pt -> pt.replaceTypeParameter(this, tpi)).toList();
        tpi.builder().setTypeBounds(newBounds);
        return tpi;
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

        @Override
        public List<ParameterizedType> getTypeBounds() {
            return typeBounds;
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
        assert typeBounds.isSet() : "Have no type bounds for " + this;
        return typeBounds.get();
    }

    @Override
    public ParameterizedType asParameterizedType() {
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

    @Override
    public String toString() {
        String shortOwner = owner.isLeft() ? owner.getLeft().simpleName()
                : owner.getRight().typeInfo().simpleName() + "." + owner.getRight().name();
        return simpleName() + "=TP#" + index + " in " + shortOwner;
    }

    @Override
    public String toStringWithTypeBounds() {
        return this + " " + typeBounds.get();
    }

    @Override
    public List<AnnotationExpression> annotations() {
        return annotations;
    }
}
