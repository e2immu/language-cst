package org.e2immu.language.cst.api.output;

public interface OutputElement {

    String minimal();

    String write(FormattingOptions options);

    default int length(FormattingOptions options) {
        return write(options).length();
    }

    /**
     * used for testing only
     *
     * @return Java-code that can be pasted into a test
     */
    String generateJavaForDebugging();
}
