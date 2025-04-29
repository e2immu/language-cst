package org.e2immu.language.cst.api.element;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Set;

public interface SourceSet {

    Charset encoding();

    String name();

    /**
     * If this source set represents sources, this path points to a directory structure that contain the sources.
     * In the case of Java, the directory structure must be compatible with the package of the compilation unit.
     * <p>
     * If this source set represents an external library, this path is either <code>null</code>, or points to
     * the directory where sources have been expanded or computed using a decompiler.
     * The location of the library (jar) will be part of extensions of this object (<code>ClassPathPart</code>).
     * <p>
     * @return a path representing a directory containing source files
     */
    Path path();

    boolean test();

    boolean library();

    boolean externalLibrary();

    boolean partOfJdk();

    Set<String> restrictToPackages();

    // which sourceSets must be present for this source set to compile/run/resolve?
    Set<SourceSet> dependencies();

    /**
     * Used to determine whether the source of any of the types in this source set has changed.
     * Throws an error when not yet set.
     * <p>
     * The value may be computed from the sources in the <code>path</code>, or from any jar file that is their origin.
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

    // helper methods

    boolean acceptSource(String packageName, String typeName);
}
