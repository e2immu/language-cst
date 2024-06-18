package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;

public interface BreakStatement extends BreakOrContinueStatement {

    interface Builder extends Statement.Builder<Builder> {
        @Fluent
        Builder setGoToLabel(String goToLabel);

        BreakStatement build();
    }

}
