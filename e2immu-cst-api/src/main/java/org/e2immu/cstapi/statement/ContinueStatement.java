package org.e2immu.cstapi.statement;

import org.e2immu.annotation.Fluent;

public interface ContinueStatement extends BreakOrContinueStatement {

    interface Builder extends Statement.Builder<Builder> {
        @Fluent
        Builder setGoToLabel(String goToLabel);

        ContinueStatement build();
    }
}
