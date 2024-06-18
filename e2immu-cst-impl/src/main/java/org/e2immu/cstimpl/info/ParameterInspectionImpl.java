package org.e2immu.cstimpl.info;


import org.e2immu.annotation.Fluent;
import org.e2immu.language.cst.api.info.ParameterInfo;

public class ParameterInspectionImpl extends InspectionImpl implements ParameterInspection {

    private final boolean varArgs;

    public ParameterInspectionImpl(Inspection inspection, boolean varArgs) {
        super(inspection.access(), inspection.comments(), inspection.source(), inspection.isSynthetic(),
                inspection.annotations());
        this.varArgs = varArgs;
    }

    @Override
    public boolean isVarArgs() {
        return varArgs;
    }

    public static class Builder extends InspectionImpl.Builder <ParameterInfo.Builder>
            implements ParameterInspection, ParameterInfo.Builder {
        private boolean varArgs;
        private final ParameterInfoImpl parameterInfo;

        public Builder(ParameterInfoImpl parameterInfo) {
            this.parameterInfo = parameterInfo;
        }

        @Override
        public boolean isVarArgs() {
            return varArgs;
        }

        @Override
        public boolean hasBeenCommitted() {
            return parameterInfo.hasBeenCommitted();
        }

        @Override
        public Builder computeAccess() {
            throw new UnsupportedOperationException("there are no access modifiers for parameters");
        }

        @Override
        public void commit() {
            ParameterInspection pi = new ParameterInspectionImpl(this, varArgs);
            parameterInfo.commit(pi);
        }

        @Fluent
        public Builder setVarArgs(boolean varArgs) {
            this.varArgs = varArgs;
            return this;
        }
    }
}
