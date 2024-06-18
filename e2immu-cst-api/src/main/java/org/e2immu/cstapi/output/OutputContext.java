package org.e2immu.cstapi.output;

public interface OutputContext {
    FormattingOptions defaultFormattingOptions();

    Formatter newFormatter(FormattingOptions formattingOptions);

    Qualification qualificationFQN();

    Qualification doNotQualifyImplicit();
}
