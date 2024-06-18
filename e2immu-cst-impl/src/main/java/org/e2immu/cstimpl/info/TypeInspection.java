package org.e2immu.cstimpl.info;

import org.e2immu.cstapi.info.FieldInfo;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.info.TypeModifier;
import org.e2immu.cstapi.type.ParameterizedType;
import org.e2immu.cstapi.type.TypeNature;
import org.e2immu.cstapi.type.TypeParameter;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface TypeInspection extends Inspection {

    List<FieldInfo> fields();

    List<TypeParameter> typeParameters();

    boolean isAbstract();

    enum MethodsEnum implements TypeInfo.Methods {

        THIS_TYPE_ONLY(false, false, null),
        THIS_TYPE_ONLY_EXCLUDE_FIELD_SAM(false, false, null),
        THIS_TYPE_ONLY_EXCLUDE_FIELD_ARTIFICIAL_SAM(false, false, null),
        INCLUDE_SUBTYPES(true, false, THIS_TYPE_ONLY),
        INCLUDE_SUPERTYPES(false, true, THIS_TYPE_ONLY);

        MethodsEnum(boolean recurseIntoSubTypes, boolean recurseIntoSuperTypes, TypeInfo.Methods nonRecursiveVariant) {
            this.recurseIntoSubTypes = recurseIntoSubTypes;
            this.recurseIntoSuperTypes = recurseIntoSuperTypes;
            this.nonRecursiveVariant = nonRecursiveVariant;
        }

        final boolean recurseIntoSubTypes;
        final boolean recurseIntoSuperTypes;
        final TypeInfo.Methods nonRecursiveVariant;
    }

    Stream<MethodInfo> methodStream(TypeInfo.Methods methods);

    List<MethodInfo> constructors();

    TypeNature typeNature();

    /**
     * only returns null for JLO, primitives.
     *
     * @return the parent of this type
     */
    ParameterizedType parentClass();

    List<ParameterizedType> interfacesImplemented();

    MethodInfo singleAbstractMethod();

    List<TypeInfo> subTypes();
    
    Set<TypeModifier> modifiers();
}
