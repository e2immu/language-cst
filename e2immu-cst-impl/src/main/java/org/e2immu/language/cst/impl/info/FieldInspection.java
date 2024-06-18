package org.e2immu.language.cst.impl.info;

import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.info.FieldModifier;

import java.util.Set;

public interface FieldInspection extends Inspection {

    Expression initializer();

    Set<FieldModifier> fieldModifiers();

}
