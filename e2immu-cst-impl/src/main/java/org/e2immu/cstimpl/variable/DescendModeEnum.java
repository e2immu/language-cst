package org.e2immu.cstimpl.variable;

import org.e2immu.cstapi.variable.DescendMode;

public enum DescendModeEnum implements DescendMode {
    YES,
    NO;

    @Override
    public boolean isNo() {
        return NO == this;
    }

    @Override
    public boolean isYes() {
        return YES == this;
    }
}
