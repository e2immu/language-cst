package org.e2immu.cstapi.output;

import java.util.List;
import java.util.stream.Stream;

/**
 * Interface to hold the output that's being accumulated while printing.
 */
public interface OutputBuilder {

    default OutputBuilder addIfNotNull(OutputBuilder outputBuilder) {
        return outputBuilder != null ? add(outputBuilder) : this;
    }

    OutputBuilder add(OutputElement... outputElements);

    OutputBuilder add(OutputBuilder... outputBuilders);

    List<OutputElement> list();

    Stream<OutputElement> stream();

    boolean isEmpty();

    boolean notStart();
}
