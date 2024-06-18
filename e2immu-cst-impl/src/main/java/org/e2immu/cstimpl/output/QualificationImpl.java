package org.e2immu.cstimpl.output;

import org.e2immu.cstapi.info.FieldInfo;
import org.e2immu.cstapi.info.MethodInfo;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.output.TypeNameRequired;
import org.e2immu.cstapi.variable.FieldReference;
import org.e2immu.cstapi.variable.This;
import org.e2immu.cstapi.variable.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QualificationImpl implements Qualification {
    public static final Qualification FULLY_QUALIFIED_NAMES = new QualificationImpl(false, TypeNameImpl.Required.FQN);
    public static final Qualification SIMPLE_NAMES = new QualificationImpl(false, TypeNameImpl.Required.SIMPLE);

    private final TypeNameRequired typeNameRequired;
    private final Set<FieldInfo> unqualifiedFields = new HashSet<>();
    private final Set<MethodInfo> unqualifiedMethods = new HashSet<>();
    private final Set<This> unqualifiedThis = new HashSet<>();
    private final Map<TypeInfo, TypeNameImpl.Required> typesNotImported;
    private final Set<String> simpleTypeNames;
    private final QualificationImpl parent;
    private final QualificationImpl top;
    private final boolean doNotQualifyImplicit;

    public QualificationImpl(boolean doNotQualifyImplicit, TypeNameRequired typeNameRequired) {
        parent = null;
        top = this;
        this.typeNameRequired = typeNameRequired;
        typesNotImported = new HashMap<>();
        simpleTypeNames = new HashSet<>();
        this.doNotQualifyImplicit = doNotQualifyImplicit;
    }

    public QualificationImpl(boolean doNotQualifyImplicit, Qualification parent, TypeNameRequired typeNameRequired) {
        this.parent = (QualificationImpl) parent;
        top = ((QualificationImpl) parent).top;
        typesNotImported = null;
        simpleTypeNames = null;
        this.typeNameRequired = typeNameRequired;
        this.doNotQualifyImplicit = doNotQualifyImplicit;
    }

    @Override
    public boolean isFullyQualifiedNames() {
        return this == FULLY_QUALIFIED_NAMES;
    }

    @Override
    public TypeNameRequired qualifierRequired(TypeInfo typeInfo) {
        if (typeNameRequired != null) return typeNameRequired;
        assert top.typesNotImported != null; // to keep IntelliJ happy
        return top.typesNotImported.getOrDefault(typeInfo, TypeNameImpl.Required.SIMPLE);
    }

    @Override
    public boolean qualifierRequired(MethodInfo methodInfo) {
        if (unqualifiedMethods.contains(methodInfo)) return false;
        return parent == null || parent.qualifierRequired(methodInfo);
    }

    @Override
    public boolean doNotQualifyImplicit() {
        return doNotQualifyImplicit;
    }

    @Override
    public boolean qualifierRequired(Variable variable) {
        if (variable instanceof FieldReference fieldReference) {
            if (unqualifiedFields.contains(fieldReference.fieldInfo())) return false;
            return parent == null || parent.qualifierRequired(variable);
        }
        if (variable instanceof This thisVar) {
            QualificationImpl levelWithData = this;
            while (levelWithData.unqualifiedThis.isEmpty()) {
                levelWithData = levelWithData.parent;
                if (levelWithData == null)
                    return false; // we did not start properly at the top, we're e.g. only outputting a method
            }
            return !levelWithData.unqualifiedThis.contains(thisVar);
        }
        return false;
    }

    public void addField(FieldInfo fieldInfo) {
        boolean newName = unqualifiedFields.stream().noneMatch(fi -> fi.name().equals(fieldInfo.name()));
        if (newName) {
            unqualifiedFields.add(fieldInfo);
        } // else: we'll have to qualify, because the name has already been taken
    }

    public void addThis(This thisVar) {
        unqualifiedThis.add(thisVar);
    }

    public void addMethodUnlessOverride(MethodInfo methodInfo) {
        boolean newMethod = unqualifiedMethods.stream().noneMatch(mi -> mi.isOverloadOf(methodInfo));
        if (newMethod) {
            unqualifiedMethods.add(methodInfo);
        }
    }

    public boolean addTypeReturnImport(TypeInfo typeInfo) {
        assert parent == null; // only add these at the top level
        assert typesNotImported != null; // to keep IntelliJ happy
        assert simpleTypeNames != null;
        // IMPROVE also code for subtypes!
        if (simpleTypeNames.contains(typeInfo.simpleName())) {
            typesNotImported.put(typeInfo, TypeNameImpl.Required.FQN);
            return false;
        } else {
            simpleTypeNames.add(typeInfo.simpleName());
            return true;
        }
    }
}
