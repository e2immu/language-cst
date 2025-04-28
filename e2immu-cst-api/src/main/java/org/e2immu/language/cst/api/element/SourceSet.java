package org.e2immu.language.cst.api.element;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Set;

public interface SourceSet {

    Charset encoding();

    String name();

    Path path();

    boolean test();

    boolean library();

    boolean externalLibrary();

    boolean partOfJdk();

    Set<String> excludePackages();

    // which sourceSets must be present for this source set to compile/run/resolve?
    Set<SourceSet> dependencies();

    /**
     * Used to determine whether the source of any of the types in this source set has changed.
     * Throws an error when not yet set.
     */
    FingerPrint fingerPrint();

    /**
     * can be set only once.
     *
     * @param fingerPrint the source fingerprint
     */
    void setFingerPrint(FingerPrint fingerPrint);

    /**
     * Used to determine whether the analysis of the whole source set has changed.
     * If so, dependent source sets may have to be re-analyzed.
     * This is typically implemented using a setOnce object since the source set has to be in place before
     * the analysis can take place.
     */
    FingerPrint analysisFingerPrintOrNull();

    void setAnalysisFingerPrint(FingerPrint fingerPrint);
}
