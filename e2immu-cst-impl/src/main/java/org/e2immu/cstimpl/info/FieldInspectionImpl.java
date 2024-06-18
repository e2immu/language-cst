package org.e2immu.cstimpl.info;


import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.info.Access;
import org.e2immu.cstapi.info.FieldInfo;
import org.e2immu.cstapi.info.FieldModifier;

import java.util.HashSet;
import java.util.Set;


public class FieldInspectionImpl extends InspectionImpl implements FieldInspection {
    private final Set<FieldModifier> fieldModifiers;
    private final Expression initializer;

    public FieldInspectionImpl(Inspection inspection, Set<FieldModifier> fieldModifiers, Expression initializer) {
        super(inspection.access(), inspection.comments(), inspection.source(), inspection.isSynthetic(),
                inspection.annotations());
        this.fieldModifiers = fieldModifiers;
        this.initializer = initializer;
    }

    @Override
    public Expression initializer() {
        return initializer;
    }


    @Override
    public Set<FieldModifier> fieldModifiers() {
        return fieldModifiers;
    }

    public static class Builder extends InspectionImpl.Builder<FieldInfo.Builder> implements FieldInfo.Builder, FieldInspection {
        private final FieldInfoImpl fieldInfo;
        private final Set<FieldModifier> fieldModifiers = new HashSet<>();
        private Expression initializer;

        public Builder(FieldInfoImpl fieldInfo) {
            this.fieldInfo = fieldInfo;
        }

        @Override
        public Builder computeAccess() {
            Access fromType = fieldInfo.owner().access();
            Access fromModifier = accessFromFieldModifier();
            Access combined = fromModifier.combine(fromType);
            setAccess(combined);
            return this;
        }

        private Access accessFromFieldModifier() {
            for (FieldModifier fieldModifier : fieldModifiers) {
                if (fieldModifier.isProtected()) return AccessEnum.PROTECTED;
                if (fieldModifier.isPrivate()) return AccessEnum.PRIVATE;
                if (fieldModifier.isPublic()) return AccessEnum.PUBLIC;
            }
            return AccessEnum.PACKAGE;
        }

        @Override
        public Builder addFieldModifier(FieldModifier fieldModifier) {
            fieldModifiers.add(fieldModifier);
            return this;
        }

        @Override
        public Builder setInitializer(Expression initializer) {
            this.initializer = initializer;
            return this;
        }

        @Override
        public void commit() {
            fieldInfo.commit(new FieldInspectionImpl(this, Set.copyOf(fieldModifiers), initializer));
        }

        @Override
        public Expression initializer() {
            return initializer;
        }

        @Override
        public Set<FieldModifier> fieldModifiers() {
            return fieldModifiers;
        }

        @Override
        public boolean hasBeenCommitted() {
            return fieldInfo.hasBeenCommitted();
        }
    }
}
