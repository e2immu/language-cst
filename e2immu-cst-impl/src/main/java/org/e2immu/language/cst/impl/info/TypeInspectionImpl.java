package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.language.cst.api.type.TypeNature;
import org.e2immu.language.cst.api.type.TypeParameter;

import java.util.*;
import java.util.stream.Stream;

public class TypeInspectionImpl extends InspectionImpl implements TypeInspection {
    private final Set<TypeModifier> typeModifiers;
    private final List<MethodInfo> methods;
    private final List<MethodInfo> constructors;
    private final List<FieldInfo> fields;
    private final ParameterizedType parentClass;
    private final TypeNature typeNature;
    private final MethodInfo singleAbstractMethod;
    private final List<ParameterizedType> interfacesImplemented;
    private final List<TypeParameter> typeParameters;
    private final List<TypeInfo> subTypes;
    private final boolean fieldsAccessedInRestOfPrimaryType;
    private final MethodInfo enclosingMethod;
    private final List<TypeInfo> permittedWhenSealed;
    private final Set<TypeInfo> superTypesExcludingJavaLangObject;

    public TypeInspectionImpl(Inspection inspection,
                              Set<TypeModifier> typeModifiers,
                              List<MethodInfo> methods,
                              List<MethodInfo> constructors,
                              List<FieldInfo> fields,
                              ParameterizedType parentClass,
                              TypeNature typeNature,
                              MethodInfo singleAbstractMethod,
                              List<ParameterizedType> interfacesImplemented,
                              List<TypeParameter> typeParameters,
                              List<TypeInfo> subTypes,
                              boolean fieldsAccessedInRestOfPrimaryType,
                              MethodInfo enclosingMethod,
                              List<TypeInfo> permittedWhenSealed,
                              Set<TypeInfo> superTypesExcludingJavaLangObject) {
        super(inspection.access(), inspection.comments(), inspection.source(), inspection.isSynthetic(), inspection.annotations());
        this.typeModifiers = typeModifiers;
        this.methods = methods;
        this.constructors = constructors;
        this.parentClass = parentClass;
        this.typeNature = typeNature;
        this.singleAbstractMethod = singleAbstractMethod;
        this.interfacesImplemented = interfacesImplemented;
        this.subTypes = subTypes;
        this.fields = fields;
        this.typeParameters = typeParameters;
        this.fieldsAccessedInRestOfPrimaryType = fieldsAccessedInRestOfPrimaryType;
        this.enclosingMethod = enclosingMethod;
        this.permittedWhenSealed = permittedWhenSealed;
        this.superTypesExcludingJavaLangObject = superTypesExcludingJavaLangObject;
    }

    @Override
    public Set<TypeInfo> superTypesExcludingJavaLangObject() {
        return superTypesExcludingJavaLangObject;
    }

    @Override
    public List<TypeInfo> permittedWhenSealed() {
        return permittedWhenSealed;
    }

    @Override
    public List<TypeParameter> typeParameters() {
        return typeParameters;
    }

    @Override
    public Set<TypeModifier> modifiers() {
        return typeModifiers;
    }

    @Override
    public Stream<MethodInfo> methodStream(TypeInfo.Methods methods) {
        return this.methods.stream();
    }

    @Override
    public List<MethodInfo> constructors() {
        return constructors;
    }

    @Override
    public TypeNature typeNature() {
        return typeNature;
    }

    @Override
    public ParameterizedType parentClass() {
        return parentClass;
    }

    @Override
    public List<ParameterizedType> interfacesImplemented() {
        return interfacesImplemented;
    }

    @Override
    public MethodInfo singleAbstractMethod() {
        return singleAbstractMethod;
    }

    @Override
    public List<TypeInfo> subTypes() {
        return subTypes;
    }

    @Override
    public List<FieldInfo> fields() {
        return fields;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract(typeNature, typeModifiers);
    }

    public static class Builder extends InspectionImpl.Builder<TypeInfo.Builder> implements TypeInspection, TypeInfo.Builder {
        private final Set<TypeModifier> typeModifiers = new HashSet<>();
        private final List<MethodInfo> methods = new ArrayList<>();
        private final List<MethodInfo> constructors = new ArrayList<>();
        private final List<FieldInfo> fields = new ArrayList<>();
        private final List<ParameterizedType> interfacesImplemented = new ArrayList<>();
        private final List<TypeInfo> subTypes = new ArrayList<>();
        private final List<TypeParameter> typeParameters = new ArrayList<>();
        private final List<TypeInfo> permittedWhenSealed = new ArrayList<>();
        private final Set<TypeInfo> superTypesExcludingJavaLangObject = new HashSet<>();

        private ParameterizedType parentClass;
        private TypeNature typeNature;
        private MethodInfo singleAbstractMethod;
        private final TypeInfoImpl typeInfo;
        private boolean fieldsAccessedInRestOfPrimaryType;
        private MethodInfo enclosingMethod;

        @Override
        public Set<TypeInfo> superTypesExcludingJavaLangObject() {
            return superTypesExcludingJavaLangObject;
        }

        @Override
        public List<TypeInfo> permittedWhenSealed() {
            return permittedWhenSealed;
        }

        @Override
        public boolean fieldsAccessedInRestOfPrimaryType() {
            return fieldsAccessedInRestOfPrimaryType;
        }

        public Builder(TypeInfoImpl typeInfo) {
            this.typeInfo = typeInfo;
        }

        @Override
        public Builder setSingleAbstractMethod(MethodInfo singleAbstractMethod) {
            this.singleAbstractMethod = singleAbstractMethod;
            return this;
        }

        @Override
        public TypeInfo.Builder addSubType(TypeInfo subType) {
            subTypes.add(subType);
            return this;
        }

        @Override
        public TypeInfo.Builder addTypeModifier(TypeModifier typeModifier) {
            typeModifiers.add(typeModifier);
            return this;
        }

        @Override
        public List<TypeParameter> typeParameters() {
            return typeParameters;
        }

        @Override
        public Set<TypeModifier> modifiers() {
            return typeModifiers;
        }

        @Override
        public TypeInfo.Builder addMethod(MethodInfo methodInfo) {
            methods.add(methodInfo);
            return this;
        }

        @Override
        public TypeInfo.Builder addConstructor(MethodInfo constructor) {
            constructors.add(constructor);
            return this;
        }

        @Override
        public TypeInfo.Builder addField(FieldInfo field) {
            fields.add(field);
            return this;
        }

        @Override
        public List<FieldInfo> fields() {
            return fields;
        }

        @Override
        public TypeInfo.Builder setTypeNature(TypeNature typeNature) {
            this.typeNature = typeNature;
            return this;
        }

        @Override
        public TypeInfo.Builder setParentClass(ParameterizedType parentClass) {
            this.parentClass = parentClass;
            if (parentClass != null && !parentClass.isJavaLangObject()) {
                this.superTypesExcludingJavaLangObject.add(parentClass.typeInfo());
                this.superTypesExcludingJavaLangObject.addAll(parentClass.typeInfo().superTypesExcludingJavaLangObject());
            }
            return this;
        }

        @Override
        public TypeInfo.Builder addInterfaceImplemented(ParameterizedType interfaceImplemented) {
            this.interfacesImplemented.add(interfaceImplemented);
            this.superTypesExcludingJavaLangObject.add(interfaceImplemented.typeInfo());
            this.superTypesExcludingJavaLangObject.addAll(interfaceImplemented.typeInfo().superTypesExcludingJavaLangObject());
            return this;
        }

        @Override
        public TypeInfo.Builder addTypeParameter(TypeParameter typeParameter) {
            typeParameters.add(typeParameter);
            return this;
        }

        @Override
        public TypeInfo.Builder addPermittedType(TypeInfo typeInfo) {
            permittedWhenSealed.add(typeInfo);
            return this;
        }

        @Override
        public void commit() {
            List<TypeInfo> sortedSubTypes = subTypes.stream().sorted(Comparator.comparing(TypeInfo::simpleName)).toList();
            TypeInspection ti = new TypeInspectionImpl(this, Set.copyOf(typeModifiers), List.copyOf(methods),
                    List.copyOf(constructors), List.copyOf(fields), parentClass, typeNature, singleAbstractMethod,
                    List.copyOf(interfacesImplemented), List.copyOf(typeParameters), sortedSubTypes,
                    fieldsAccessedInRestOfPrimaryType, enclosingMethod, List.copyOf(permittedWhenSealed),
                    Set.copyOf(superTypesExcludingJavaLangObject));
            typeInfo.commit(ti);
        }

        @Override
        public Stream<MethodInfo> methodStream(TypeInfo.Methods methods) {
            return this.methods.stream(); // FIXME
        }

        @Override
        public List<MethodInfo> constructors() {
            return constructors;
        }

        @Override
        public TypeNature typeNature() {
            return typeNature;
        }

        @Override
        public ParameterizedType parentClass() {
            return parentClass;
        }

        @Override
        public List<ParameterizedType> interfacesImplemented() {
            return interfacesImplemented;
        }

        @Override
        public MethodInfo singleAbstractMethod() {
            return singleAbstractMethod;
        }

        @Override
        public List<TypeInfo> subTypes() {
            return subTypes;
        }

        @Override
        public boolean isAbstract() {
            return TypeInspectionImpl.isAbstract(typeNature, typeModifiers);
        }

        @Override
        public List<MethodInfo> methods() {
            return methods;
        }

        @Override
        public Builder computeAccess() {
            Access fromModifiers = accessFromModifiers();
            if (typeInfo.compilationUnitOrEnclosingType().isLeft()) {
                setAccess(fromModifiers);
            } else {
                Access fromEnclosing = typeInfo.compilationUnitOrEnclosingType().getRight().access();
                Access combined = fromEnclosing.combine(fromModifiers);
                setAccess(combined);
            }
            return this;
        }

        private Access accessFromModifiers() {
            for (TypeModifier typeModifier : typeModifiers) {
                if (typeModifier.isPublic()) return AccessEnum.PUBLIC;
                if (typeModifier.isPrivate()) return AccessEnum.PRIVATE;
                if (typeModifier.isProtected()) return AccessEnum.PROTECTED;
            }
            return AccessEnum.PACKAGE;
        }

        @Override
        public boolean hasBeenCommitted() {
            return typeInfo.hasBeenInspected();
        }

        @Override
        public Builder setEnclosingMethod(MethodInfo enclosingMethod) {
            this.enclosingMethod = enclosingMethod;
            return this;
        }

        @Override
        public MethodInfo enclosingMethod() {
            return enclosingMethod;
        }
    }

    @Override
    public boolean fieldsAccessedInRestOfPrimaryType() {
        return fieldsAccessedInRestOfPrimaryType;
    }

    private static boolean isAbstract(TypeNature typeNature, Set<TypeModifier> typeModifiers) {
        return typeNature.isInterface() || typeModifiers.stream().anyMatch(TypeModifier::isAbstract);
    }

    @Override
    public MethodInfo enclosingMethod() {
        return enclosingMethod;
    }
}
