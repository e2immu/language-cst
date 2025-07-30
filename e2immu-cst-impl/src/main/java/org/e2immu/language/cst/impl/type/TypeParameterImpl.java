package org.e2immu.language.cst.impl.type;

import org.e2immu.language.cst.api.analysis.PropertyValueMap;
import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.language.cst.api.info.MethodInfo;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.type.TypeParameter;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.analysis.PropertyValueMapImpl;
import org.e2immu.language.cst.impl.element.ElementImpl;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.support.Either;
import org.e2immu.support.FirstThen;
import org.e2immu.support.SetOnce;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TypeParameterImpl extends ElementImpl implements TypeParameter {
    private final int index;
    private final String name;
    private final Either<TypeInfo, MethodInfo> owner;
    private final FirstThen<Builder, List<ParameterizedType>> typeBounds;
    private final List<AnnotationExpression> annotations;
    private final List<Comment> comments;
    private final SetOnce<Source> source = new SetOnce<>();
    private final PropertyValueMap analysis = new PropertyValueMapImpl();

    public TypeParameterImpl(List<Comment> comments, List<AnnotationExpression> annotations,
                             int index, String name, Either<TypeInfo, MethodInfo> owner) {
        this.index = index;
        this.name = name;
        this.owner = owner;
        this.typeBounds = new FirstThen<>(new Builder(this));
        this.comments = comments;
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
    public int complexity() {
        return 0;
    }

    @Override
    public List<Comment> comments() {
        return comments;
    }

    @Override
    public TypeParameter rewire(InfoMap infoMap) {
        Either<TypeInfo, MethodInfo> rewiredOwner = owner.isLeft()
                ? Either.left(infoMap.typeInfo(owner.getLeft()))
                : Either.right(infoMap.methodInfo(owner.getRight()));
        List<AnnotationExpression> rewiredAnnotations = annotations.stream()
                .map(ae -> (AnnotationExpression) ae.rewire(infoMap)).toList();
        TypeParameter rewired = new TypeParameterImpl(comments, rewiredAnnotations, index, name, rewiredOwner);
        typeBounds.get().forEach(pt -> rewired.builder().addTypeBound(pt.rewire(infoMap)));
        rewired.builder().setSource(source()).commit();
        return rewired;
    }

    @Override
    public Source source() {
        return source.getOrDefaultNull();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Visitor visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.empty();
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
        TypeParameterImpl tpi = new TypeParameterImpl(comments, annotations, getIndex(), simpleName(), owner);
        List<ParameterizedType> newBounds = typeBounds().stream()
                .map(pt -> pt.replaceTypeParameter(this, tpi)).toList();
        tpi.builder().setTypeBounds(newBounds).setSource(source());
        return tpi;
    }

    @Override
    public TypeParameter.Builder builder() {
        return typeBounds.getFirst();
    }

    @Override
    public boolean typeBoundsAreSet() {
        if (typeBounds.isFirst()) return false;
        return typeBounds.get().stream().allMatch(pt -> pt.typeBoundsAreSet(this));
    }

    public static class Builder extends ElementImpl.Builder<TypeParameter.Builder> implements TypeParameter.Builder {
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
            if (source != null) {
                typeParameter.source.set(source);
            }
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
    public OutputBuilder print(Qualification qualification) {
        return print(qualification, true);
    }

    @Override
    public OutputBuilder print(Qualification qualification, boolean printTypeBounds) {
        OutputBuilder outputBuilder = new OutputBuilderImpl();
        List<AnnotationExpression> allAnnotations = Stream.concat(annotations.stream(),
                        qualification.decorator() == null ? Stream.of()
                                : qualification.decorator().annotations(this).stream())
                .toList();
        if (!allAnnotations.isEmpty()) {
            OutputBuilder ob = allAnnotations.stream().map(ae -> ae.print(qualification))
                    .collect(OutputBuilderImpl.joining(SpaceEnum.ONE));
            outputBuilder.add(ob).add(SpaceEnum.ONE);
        }
        outputBuilder.add(new TextImpl(name));
        if (typeBounds.isSet() && !typeBounds.get().isEmpty() && printTypeBounds) {
            outputBuilder.add(SpaceEnum.ONE).add(KeywordImpl.EXTENDS).add(SpaceEnum.ONE);
            outputBuilder.add(typeBounds.get()
                    .stream()
                    .map(pt -> ParameterizedTypePrinter.print(qualification, pt, false,
                            DiamondEnum.SHOW_ALL, false, false))
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

    @Override
    public Stream<Element.TypeReference> typesReferenced(boolean explicit, Set<TypeParameter> visited) {
        if (visited.add(this)) {
            Stream<Element.TypeReference> s1 = annotations().stream().flatMap(Element::typesReferenced);
            Stream<Element.TypeReference> s2 = typeBounds().stream().flatMap(pt -> pt.typesReferenced(explicit, visited));
            return Stream.concat(s1, s2);
        }
        return Stream.of();
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return typesReferenced(true, new HashSet<>());
    }

    @Override
    public PropertyValueMap analysis() {
        return analysis;
    }
}
