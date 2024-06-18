package org.e2immu.cstapi.type;

import org.e2immu.annotation.Fluent;
import org.e2immu.annotation.NotNull;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.support.Either;

import java.util.List;
import java.util.Set;

public interface TypeParameter extends NamedType {

    int getIndex();

    Either<TypeInfo, MethodInfo> getOwner();

    default boolean isMethodTypeParameter() {
        return getOwner().isRight();
    }

    List<ParameterizedType> typeBounds();

    @NotNull
    OutputBuilder print(Qualification qualification, Set<TypeParameter> visitedTypeParameters);

    default TypeInfo primaryType() {
        return getOwner().isLeft() ? getOwner().getLeft().primaryType() : getOwner().getRight().primaryType();
    }

    Builder builder();

    interface Builder {
        @Fluent
        Builder setTypeBounds(List<ParameterizedType> typeBounds);

        @Fluent
        Builder addTypeBound(ParameterizedType typeBound);

        // when all type bounds have been set
        TypeParameter commit();
    }
}
