package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.JavaDoc;
import org.e2immu.language.cst.api.element.Source;

public class JavaDocImpl extends MultiLineComment implements JavaDoc {
    public JavaDocImpl(Source source, String comment) {
        super(source, comment);
    }
}
