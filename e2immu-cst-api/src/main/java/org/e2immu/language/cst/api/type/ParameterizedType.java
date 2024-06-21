package org.e2immu.language.cst.api.type;

import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.runtime.Factory;
import org.e2immu.language.cst.api.runtime.Predefined;
import org.e2immu.language.cst.api.runtime.PredefinedWithoutParameterizedType;
import org.e2immu.language.cst.api.runtime.Predefined;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface ParameterizedType {
    ParameterizedType applyTranslation(PredefinedWithoutParameterizedType predefined,
                                       Map<NamedType, ParameterizedType> translate);

    int arrays();

    // initially implemented to ensure that a NULL type doesn't overwrite a valid type, see Lambda_17
    ParameterizedType bestDefined(ParameterizedType other);
    // the different components

    /**
     * @return null in case of an unbound type parameter, in which case "JLO" can be understood.
     */
    TypeInfo bestTypeInfo();

    ParameterizedType concreteDirectSuperType(Predefined runtime, ParameterizedType interfaceImplemented);

    ParameterizedType concreteSuperType(Predefined runtime, ParameterizedType superType);

    ParameterizedType copyWithArrays(int arrays);

    ParameterizedType copyWithFewerArrays(int n);

    ParameterizedType copyWithOneFewerArrays();

    // identification

    ParameterizedType copyWithoutArrays();

    // print

    String detailedString();

    ParameterizedType ensureBoxed(Predefined runtime);

    // conversions

    ParameterizedType ensureBoxed(PredefinedWithoutParameterizedType predefined);

    boolean equalsIgnoreArrays(ParameterizedType other);

    ParameterizedType erased();

    Set<TypeParameter> extractTypeParameters();

    /*
    HashMap<K, V> implements Map<K, V>
    Given Map<K, V>, go from abstract to concrete (HM:K to Map:K, HM:V to Map:V)
    */
    Map<NamedType, ParameterizedType> forwardTypeParameterMap(Predefined runtime);

    String fullyQualifiedName();

    /*
       Given a concrete type (List<String>) make a map from the type's abstract parameters to its concrete ones (E -> String)

       If the abstract type contains self-references, we cannot recurse, because their type parameters have the same name...
       With visited, the method returns K=Integer, V=Map<Integer,String> when presented with Map<Integer,Map<Integer,String>>,
       without visited, it would recurse and return K=Integer, V=String
       */
    Map<NamedType, ParameterizedType> initialTypeParameterMap(Predefined runtime);

    // simple checks

    // TODO consider moving this to "runtime"
    boolean isAssignableFrom(Predefined runtime, ParameterizedType other);

    boolean isBoolean();

    boolean isBooleanOrBoxedBoolean();

    boolean isBoxedExcludingVoid();

    boolean isFunctionalInterface();

    boolean isInt();

    boolean isJavaLangObject();

    boolean isJavaLangString();

    boolean isMathematicallyInteger();

    boolean isNoTypeGivenInLambda();

    boolean isNumeric();

    boolean isPrimitiveExcludingVoid();

    boolean isPrimitiveStringClass();

    boolean isReturnTypeOfConstructor();

    default boolean isType() {
        return typeInfo() != null;
    }

    boolean isTypeOfNullConstant();

    boolean isTypeParameter();

    boolean isUnboundTypeParameter();

    boolean isUnboundWildcard();

    boolean isVoid();

    boolean isVoidOrJavaLangVoid();

    boolean isWILDCARD_PARAMETERIZED_TYPE();

    ParameterizedType mostSpecific(Predefined runtime,
                                   TypeInfo primaryType,
                                   ParameterizedType other);

    List<ParameterizedType> parameters();

    OutputBuilder print(Qualification qualification, boolean varArgs, Diamond diamond);

    String printForMethodFQN(boolean varArgs, Diamond diamond);

    List<ParameterizedType> replaceByTypeBounds();

    TypeInfo toBoxed(Predefined runtime);

    TypeInfo typeInfo();

    TypeParameter typeParameter();

    Stream<Element.TypeReference> typesReferenced();

    Wildcard wildcard();

    ParameterizedType withParameters(List<ParameterizedType> parameterizedTypes);

    ParameterizedType withWildcard(Wildcard wildcard);
}
