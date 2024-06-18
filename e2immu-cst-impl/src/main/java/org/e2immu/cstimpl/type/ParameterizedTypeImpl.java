package org.e2immu.cstimpl.type;

import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.runtime.PredefinedWithoutParameterizedType;
import org.e2immu.cstapi.runtime.Runtime;
import org.e2immu.cstapi.type.*;
import org.e2immu.cstimpl.element.ElementImpl;
import org.e2immu.cstimpl.output.QualificationImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParameterizedTypeImpl implements ParameterizedType {
    public static final ParameterizedType NULL_CONSTANT = new ParameterizedTypeImpl();
    public static final ParameterizedType RETURN_TYPE_OF_CONSTRUCTOR = new ParameterizedTypeImpl();
    public static final ParameterizedType NO_TYPE_GIVEN_IN_LAMBDA = new ParameterizedTypeImpl();
    public static final ParameterizedType WILDCARD_PARAMETERIZED_TYPE = new ParameterizedTypeImpl(WildcardEnum.UNBOUND);
    public static final ParameterizedType TYPE_OF_EMPTY_EXPRESSION = new ParameterizedTypeImpl();

    private final TypeParameter typeParameter;
    private final TypeInfo typeInfo;
    private final int arrays;
    private final Wildcard wildcard;
    private final List<ParameterizedType> parameters;

    public ParameterizedTypeImpl(TypeParameter typeParameter, int arrays) {
        this(null, typeParameter, List.of(), arrays, null);
    }

    public ParameterizedTypeImpl(TypeInfo typeInfo) {
        this(typeInfo, null, List.of(), 0, null);
    }

    public ParameterizedTypeImpl(TypeInfo typeInfo, int arrays) {
        this(typeInfo, null, List.of(), arrays, null);
    }

    public ParameterizedTypeImpl(TypeInfo typeInfo, List<ParameterizedType> parameters) {
        this(typeInfo, null, parameters, 0, null);
    }

    public ParameterizedTypeImpl(TypeParameter typeParameter, Wildcard wildcard) {
        this(null, typeParameter, List.of(), 0, wildcard);
    }

    public ParameterizedTypeImpl(TypeInfo typeInfo, Wildcard wildcard) {
        this(typeInfo, null, List.of(), 0, wildcard);
    }

    public ParameterizedTypeImpl(Wildcard wildcard) {
        this(null, null, List.of(), 0, wildcard);
    }

    public ParameterizedTypeImpl() {
        this(null, null, List.of(), 0, null);
    }

    public ParameterizedTypeImpl(TypeInfo typeInfo,
                                 TypeParameter typeParameter,
                                 List<ParameterizedType> parameters,
                                 int arrays,
                                 Wildcard wildcard) {
        this.typeParameter = typeParameter;
        this.typeInfo = typeInfo;
        this.arrays = arrays;
        this.wildcard = wildcard;
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterizedTypeImpl that = (ParameterizedTypeImpl) o;
        return arrays == that.arrays
               && Objects.equals(typeParameter, that.typeParameter)
               && Objects.equals(typeInfo, that.typeInfo)
               && Objects.equals(wildcard, that.wildcard)
               && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeParameter, typeInfo, arrays, wildcard, parameters);
    }

    public OutputBuilder print(Qualification qualification) {
        return ParameterizedTypePrinter.print(qualification, this,
                false, DiamondEnum.SHOW_ALL, false);
    }

    @Override
    public Wildcard wildcard() {
        return wildcard;
    }

    @Override
    public TypeParameter typeParameter() {
        return typeParameter;
    }

    @Override
    public TypeInfo typeInfo() {
        return typeInfo;
    }

    @Override
    public int arrays() {
        return arrays;
    }

    @Override
    public List<ParameterizedType> parameters() {
        return parameters;
    }

    @Override
    public String fullyQualifiedName() {
        return printForMethodFQN(false, DiamondEnum.SHOW_ALL);
    }

    public OutputBuilder print(Qualification qualification, boolean varArgs, Diamond diamond) {
        return ParameterizedTypePrinter.print(qualification, this, varArgs, diamond, false);
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.concat(Stream.ofNullable(typeInfo != null
                        ? new ElementImpl.TypeReference(typeInfo, true) : null),
                parameters.stream().flatMap(ParameterizedType::typesReferenced));
    }

    @Override
    public ParameterizedType erased() {
        if (arrays == 0 && wildcard == null && parameters.isEmpty()) return this;
        return new ParameterizedTypeImpl(typeInfo, typeParameter, List.of(), 0, null);
    }

    @Override
    public ParameterizedType copyWithArrays(int arrays) {
        // the following check is important to maintain object '==' for the static types like NULL_CONSTANT
        if (arrays == this.arrays) return this;
        assert arrays >= 0;
        return new ParameterizedTypeImpl(typeInfo, typeParameter, parameters, arrays, wildcard);
    }

    @Override
    public ParameterizedType copyWithOneFewerArrays() {
        assert arrays > 0;
        return new ParameterizedTypeImpl(typeInfo, typeParameter, parameters, arrays - 1, wildcard);
    }

    @Override
    public ParameterizedType ensureBoxed(PredefinedWithoutParameterizedType predefined) {
        return null;
    }

    @Override
    public ParameterizedType copyWithFewerArrays(int n) {
        assert arrays >= n;
        return new ParameterizedTypeImpl(typeInfo, typeParameter, parameters, arrays - n, wildcard);
    }

    @Override
    public ParameterizedType copyWithoutArrays() {
        return new ParameterizedTypeImpl(typeInfo, typeParameter, parameters, 0, wildcard);
    }

    @Override
    public boolean isBoolean() {
        return arrays == 0 && typeInfo != null && typeInfo.isBoolean();
    }

    @Override
    public boolean isTypeParameter() {
        return typeParameter != null;
    }

    @Override
    public boolean isVoidOrJavaLangVoid() {
        if (isVoid()) return true;
        return arrays == 0 && typeInfo != null && typeInfo.isJavaLangVoid();
    }

    @Override
    public boolean isBooleanOrBoxedBoolean() {
        if (arrays != 0) return false;
        return typeInfo != null && (typeInfo.isBoolean() || typeInfo.isBoxedBoolean());
    }

    @Override
    public boolean isVoid() {
        if (this == TYPE_OF_EMPTY_EXPRESSION) return true;
        return arrays == 0 && typeInfo != null && typeInfo.isVoid();
    }

    @Override
    public boolean isPrimitiveExcludingVoid() {
        return arrays == 0 && typeInfo != null && typeInfo.isPrimitiveExcludingVoid();
    }

    @Override
    public boolean isPrimitiveStringClass() {
        return arrays == 0 && typeInfo != null
               && (typeInfo.isPrimitiveExcludingVoid() || typeInfo.isJavaLangString() || typeInfo.isJavaLangClass());
    }

    @Override
    public boolean isJavaLangString() {
        return arrays == 0 && typeInfo != null && typeInfo.isJavaLangString();
    }

    @Override
    public boolean isInt() {
        return arrays == 0 && typeInfo != null && typeInfo.isInt();
    }

    @Override
    public boolean isJavaLangObject() {
        return arrays == 0 && typeInfo != null && typeInfo.isJavaLangObject();
    }

    @Override
    public boolean isFunctionalInterface() {
        if (typeInfo == null || arrays > 0) return false;
        return typeInfo.isFunctionalInterface();
    }

    @Override
    public boolean isBoxedExcludingVoid() {
        return arrays == 0 && typeInfo != null && typeInfo.isBoxedExcludingVoid();
    }

    @Override
    public boolean isWILDCARD_PARAMETERIZED_TYPE() {
        return this == WILDCARD_PARAMETERIZED_TYPE;
    }

    @Override
    public boolean isReturnTypeOfConstructor() {
        return this == RETURN_TYPE_OF_CONSTRUCTOR;
    }

    @Override
    public boolean isNumeric() {
        return arrays == 0 && typeInfo != null && typeInfo.isNumeric();
    }

    @Override
    public boolean isUnboundTypeParameter() {
        return arrays == 0
               && typeParameter != null
               && (typeParameter.typeBounds().isEmpty()
                   || typeParameter.typeBounds().size() == 1 && typeParameter.typeBounds().get(0).isJavaLangObject());
    }

    @Override
    public boolean isAssignableFrom(Runtime runtime, ParameterizedType other) {
        return new IsAssignableFrom(runtime, this, other).execute();
    }

    @Override
    public String detailedString() {
        return printForMethodFQN(false, DiamondEnum.SHOW_ALL);
    }

    @Override
    public TypeInfo toBoxed(Runtime runtime) {
        return runtime.boxed(typeInfo);
    }

    public ParameterizedType ensureBoxed(Runtime runtime) {
        if (isPrimitiveExcludingVoid() || isVoid()) {
            return toBoxed(runtime).asSimpleParameterizedType();
        }
        return this;
    }

    @Override
    public String printForMethodFQN(boolean varArgs, Diamond diamond) {
        return ParameterizedTypePrinter.print(QualificationImpl.FULLY_QUALIFIED_NAMES,
                this, varArgs, diamond, false, new HashSet<>()).toString();
    }

    @Override
    public boolean equalsIgnoreArrays(ParameterizedType other) {
        return Objects.equals(typeInfo, other.typeInfo())
               && Objects.equals(typeParameter, other.typeParameter())
               && Objects.equals(parameters, other.parameters())
               && Objects.equals(wildcard, other.wildcard());
    }

    @Override
    public boolean isTypeOfNullConstant() {
        return this == NULL_CONSTANT;
    }

    @Override
    public boolean isUnboundWildcard() {
        return typeInfo == null && typeParameter == null && wildcard != null && wildcard.isUnbound();
    }

    @Override
    public ParameterizedType concreteSuperType(Runtime runtime, ParameterizedType superType) {
        TypeInfo bestType = bestTypeInfo();
        if (bestType == superType.typeInfo()) {
            // if we start with Iterable<String>, and we're aiming for Iterable<E>, then
            // Iterable<String> is the right answer
            return this;
        }
        ParameterizedType parentClass = bestType.parentClass();
        if (parentClass != null && !parentClass.isJavaLangObject()) {
            if (parentClass.typeInfo() == superType.typeInfo()) {
                return concreteDirectSuperType(runtime, parentClass);
            }
            /* do a recursion, but accept that we may return null
            we must call concreteSuperType on a concrete version of the parentClass
            */
            ParameterizedType res = parentClass.concreteSuperType(runtime, superType);
            if (res != null) {
                return concreteDirectSuperType(runtime, res);
            }
        }
        for (ParameterizedType interfaceType : bestType.interfacesImplemented()) {
            if (interfaceType.typeInfo() == superType.typeInfo()) {
                return concreteDirectSuperType(runtime, interfaceType);
            }
            // similar to parent
            ParameterizedType res = interfaceType.concreteSuperType(runtime, superType);
            if (res != null) {
                return concreteDirectSuperType(runtime, res);
            }
        }
        return null;
    }

    @Override
    public ParameterizedType commonType(Runtime runtime, ParameterizedType other) {
        return new CommonType(runtime).commonType(this, other);
    }

    @Override
    public ParameterizedType concreteDirectSuperType(Runtime runtime, ParameterizedType parentType) {
        if (parentType.parameters().isEmpty()) return parentType;

        Map<NamedType, ParameterizedType> map = initialTypeParameterMap(runtime);
        ParameterizedType formalType = parentType.typeInfo().asParameterizedType(runtime);
        List<ParameterizedType> newParameters = new ArrayList<>(formalType.parameters().size());
        int i = 0;
        for (ParameterizedType param : formalType.parameters()) {
            ParameterizedType formalInParentType = parentType.parameters().get(i);
            ParameterizedType result;
            if (formalInParentType.typeInfo() != null) {
                result = formalInParentType;
            } else if (formalInParentType.typeParameter() != null) {
                result = map.get(formalInParentType.typeParameter());
            } else {
                result = param;
            }
            newParameters.add(result == null ? param : result);
            i++;
        }
        return new ParameterizedTypeImpl(parentType.typeInfo(), List.copyOf(newParameters));
    }

    @Override
    public Map<NamedType, ParameterizedType> initialTypeParameterMap(Runtime runtime) {
        if (!isType()) return Map.of();
        if (parameters.isEmpty()) return Map.of();
        return initialTypeParameterMap(runtime, new HashSet<>());
    }

    private Map<NamedType, ParameterizedType> initialTypeParameterMap(Runtime runtime, Set<TypeInfo> visited) {
        if (!isType()) return Map.of();
        visited.add(typeInfo);
        if (parameters.isEmpty()) return Map.of();
        ParameterizedType originalType = typeInfo.asParameterizedType(runtime);
        int i = 0;
        // linkedHashMap to maintain an order for testing
        Map<NamedType, ParameterizedType> map = new LinkedHashMap<>();
        for (ParameterizedType parameter : originalType.parameters()) {
            ParameterizedType recursive;
            if (parameter.isTypeParameter()) {
                ParameterizedType pt = parameters.get(i);
                if (pt != null && pt.isUnboundWildcard() && !parameter.typeParameter().typeBounds().isEmpty()) {
                    // replace '?' by '? extends X', with 'X' the first type bound, see TypeParameter_3
                    // but never do this for JLO (see e.g. issues described in MethodCall_73)
                    TypeInfo bound = parameter.typeParameter().typeBounds().get(0).typeInfo();
                    if (bound.isJavaLangObject()) {
                        recursive = WILDCARD_PARAMETERIZED_TYPE;
                    } else {
                        recursive = new ParameterizedTypeImpl(bound, WildcardEnum.EXTENDS);
                    }
                } else {
                    recursive = pt;
                }
                map.put(parameter.typeParameter(), recursive);
            } else if (parameter.isType()) {
                recursive = parameter;
            } else throw new UnsupportedOperationException();
            if (recursive != null && recursive.isType() && !visited.contains(recursive.typeInfo())) {
                Map<NamedType, ParameterizedType> recursiveMap = ((ParameterizedTypeImpl) recursive)
                        .initialTypeParameterMap(runtime, visited);
                map.putAll(recursiveMap);
            }
            i++;
        }
        return map;
    }

    @Override
    public String toString() {
        return (typeParameter != null ? "Type " : isTypeParameter() ? "Type param " : "") + detailedString();
    }

    @Override
    public TypeInfo bestTypeInfo() {
        if (typeInfo != null) return typeInfo;
        if (typeParameter != null) {
            if (wildcard.isExtends() && parameters.size() == 1) {
                return parameters.get(0).bestTypeInfo();
            }
            TypeParameter definition;
            if (typeParameter.getOwner() != null) {
                if (typeParameter.getOwner().isLeft()) {
                    TypeInfo owner = typeParameter.getOwner().getLeft();
                    definition = owner.typeParameters().get(typeParameter.getIndex());
                } else {
                    MethodInfo owner = typeParameter.getOwner().getRight();
                    definition = owner.typeParameters().get(typeParameter.getIndex());
                }
                if (!definition.typeBounds().isEmpty()) {
                    // IMPROVE should be a joint type
                    return definition.typeBounds().get(0).typeInfo();
                }
            } // else: in JFocus, we can temporarily have no owner during type generalization
        }
        return null;
    }

    @Override
    public ParameterizedType mostSpecific(Runtime runtime, TypeInfo primaryType, ParameterizedType other) {
        if (equals(other)) return this;
        if (isType() && typeInfo.isVoid() || other.isType() && other.typeInfo().isVoid()) {
            return runtime.voidParameterizedType();
        }
        if (isTypeParameter()) {
            if (other.isTypeParameter()) {
                // a type parameter in the primary type has priority over another one
                // IMPROVE change this to a hierarchy rather than primary vs other
                if (primaryType.equals(other.typeParameter().primaryType())) return other;
                return this;
            }
            return other;
        }
        if (other.isTypeParameter() && !isTypeParameter()) return this;

        if (isBoxedExcludingVoid() && other.isPrimitiveExcludingVoid()) return other;
        if (other.isBoxedExcludingVoid() && isPrimitiveExcludingVoid()) return this;

        if (isAssignableFrom(runtime, other)) {
            return other;
        }
        return this;
    }

    /*

    /*
    HashMap<K, V> implements Map<K, V>
    Given Map<K, V>, go from abstract to concrete (HM:K to Map:K, HM:V to Map:V)
    */
    @Override
    public Map<NamedType, ParameterizedType> forwardTypeParameterMap(Runtime runtime) {
        if (!isType()) return Map.of();
        if (parameters.isEmpty()) return Map.of();
        ParameterizedType originalType = typeInfo.asParameterizedType(runtime); // Map:K, Map:V
        assert originalType.parameters().size() == parameters.size();
        int i = 0;
        // linkedHashMap to maintain an order for testing
        Map<NamedType, ParameterizedType> map = new LinkedHashMap<>();
        for (ParameterizedType parameter : originalType.parameters()) {
            ParameterizedType p = parameters.get(i);
            if (p.isTypeParameter()) {
                map.put(p.typeParameter(), parameter);
            }
            i++;
        }
        return map;
    }

    /**
     * IMPORTANT: code copied from MethodTypeParameterMap
     *
     * @param translate the map to be applied on the type parameters of this
     * @return a newly created ParameterizedType
     */
    @Override
    public ParameterizedType applyTranslation(PredefinedWithoutParameterizedType predefined,
                                              Map<NamedType, ParameterizedType> translate) {
        return applyTranslation(predefined, translate, 0);
    }

    private ParameterizedType applyTranslation(PredefinedWithoutParameterizedType primitives,
                                               Map<NamedType, ParameterizedType> translate,
                                               int recursionDepth) {
        if (recursionDepth > 20) {
            throw new IllegalArgumentException("Reached recursion depth");
        }
        if (translate.isEmpty()) return this;
        ParameterizedType pt = this;
        if (pt.isTypeParameter()) {
            boolean add = false;
            while (pt.isTypeParameter() && translate.containsKey(pt.typeParameter())) {
                ParameterizedType newPt = translate.get(pt.typeParameter());
                if (newPt.equals(pt) || newPt.isTypeParameter() && pt.typeParameter().equals(newPt.typeParameter()))
                    break;
                pt = newPt;
                add = true;
            }
            // we want to add this.arrays only once, and only when there was a translation (MethodCall_61)
            if (add) {
                pt = pt.copyWithArrays(pt.arrays() + arrays);
            }
        }
        // see MethodCall_60,_61,_62
        final ParameterizedType stablePt = pt;
        if (stablePt.parameters().isEmpty()) return stablePt;
        List<ParameterizedType> recursivelyMappedParameters = stablePt.parameters().stream()
                .map(x -> x == stablePt || x == this
                        ? stablePt
                        : ((ParameterizedTypeImpl) x).applyTranslation(primitives, translate, recursionDepth + 1))
                .map(x -> x.ensureBoxed(primitives))
                .collect(Collectors.toList());
        if (stablePt.typeInfo() == null) {
            throw new UnsupportedOperationException("? input " + stablePt + " has no type");
        }
        return new ParameterizedTypeImpl(stablePt.typeInfo(), recursivelyMappedParameters);
    }

    @Override
    public boolean isMathematicallyInteger() {
        return arrays == 0 && typeInfo != null && (
                typeInfo.isByte() || typeInfo.isBoxedByte() ||
                typeInfo.isShort() || typeInfo.isBoxedShort() ||
                typeInfo.isInt() || typeInfo.isInteger() ||
                typeInfo.isLong() || typeInfo.isBoxedLong() ||
                typeInfo.isChar() || typeInfo.isCharacter());
    }

    @Override
    public ParameterizedType withWildcard(Wildcard wildcard) {
        return new ParameterizedTypeImpl(typeInfo, typeParameter, parameters, arrays, wildcard);
    }

    @Override
    public ParameterizedType withParameters(List<ParameterizedType> parameters) {
        return new ParameterizedTypeImpl(typeInfo, typeParameter, parameters, arrays, wildcard);
    }
}
