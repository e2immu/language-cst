package org.e2immu.language.cst.api.type;

import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.info.TypeInfo;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.runtime.PredefinedWithoutParameterizedType;
import org.e2immu.language.cst.api.runtime.Runtime;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ParameterizedType {
    // the different components

    Wildcard wildcard();

    TypeParameter typeParameter();

    TypeInfo typeInfo();

    int arrays();

    List<ParameterizedType> parameters();

    /**
     * @return null in case of an unbound type parameter, in which case "JLO" can be understood.
     */
    TypeInfo bestTypeInfo();

    // identification

    String fullyQualifiedName();

    // print

    OutputBuilder print(Qualification qualification, boolean varArgs, Diamond diamond);

    Stream<Element.TypeReference> typesReferenced();

    // conversions

    ParameterizedType ensureBoxed(Runtime runtime);

    ParameterizedType erased();

    ParameterizedType copyWithArrays(int arrays);

    ParameterizedType copyWithOneFewerArrays();

    ParameterizedType ensureBoxed(PredefinedWithoutParameterizedType predefined);

    ParameterizedType copyWithFewerArrays(int n);

    ParameterizedType copyWithoutArrays();

    // simple checks

    boolean isBoolean();

    boolean isTypeParameter();

    boolean isVoidOrJavaLangVoid();

    boolean isBooleanOrBoxedBoolean();

    boolean isVoid();

    boolean isPrimitiveExcludingVoid();

    boolean isPrimitiveStringClass();

    boolean isJavaLangString();

    boolean isInt();

    boolean isJavaLangObject();

    boolean isFunctionalInterface();

    boolean isBoxedExcludingVoid();

    boolean isWILDCARD_PARAMETERIZED_TYPE();

    boolean isReturnTypeOfConstructor();

    boolean isNumeric();

    boolean isUnboundTypeParameter();

    // TODO consider moving this to "runtime"
    boolean isAssignableFrom(Runtime runtime, ParameterizedType other);

    String detailedString();

    default boolean isType() {
        return typeInfo() != null;
    }

    TypeInfo toBoxed(Runtime runtime);

    String printForMethodFQN(boolean varArgs, Diamond diamond);

    boolean equalsIgnoreArrays(ParameterizedType other);

    boolean isTypeOfNullConstant();

    ParameterizedType concreteDirectSuperType(Runtime runtime, ParameterizedType interfaceImplemented);

    ParameterizedType concreteSuperType(Runtime runtime, ParameterizedType superType);

    ParameterizedType commonType(Runtime runtime, ParameterizedType other);

    boolean isUnboundWildcard();

    ParameterizedType mostSpecific(Runtime runtime,
                                   TypeInfo primaryType,
                                   ParameterizedType other);

    /*
       Given a concrete type (List<String>) make a map from the type's abstract parameters to its concrete ones (E -> String)

       If the abstract type contains self-references, we cannot recurse, because their type parameters have the same name...
       With visited, the method returns K=Integer, V=Map<Integer,String> when presented with Map<Integer,Map<Integer,String>>,
       without visited, it would recurse and return K=Integer, V=String
       */
    Map<NamedType, ParameterizedType> initialTypeParameterMap(Runtime runtime);

    /*
    HashMap<K, V> implements Map<K, V>
    Given Map<K, V>, go from abstract to concrete (HM:K to Map:K, HM:V to Map:V)
    */
    Map<NamedType, ParameterizedType> forwardTypeParameterMap(Runtime runtime);

    ParameterizedType applyTranslation(PredefinedWithoutParameterizedType predefined,
                                       Map<NamedType, ParameterizedType> translate);

    boolean isMathematicallyInteger();

    ParameterizedType withParameters(List<ParameterizedType> parameterizedTypes);

    ParameterizedType withWildcard(Wildcard wildcard);
}
