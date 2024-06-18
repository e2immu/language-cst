package org.e2immu.cstimpl.info;

import org.e2immu.cstapi.expression.Expression;
import org.e2immu.cstapi.info.FieldModifier;

import java.util.Set;

public interface FieldInspection extends Inspection {

    Expression initializer();

    Set<FieldModifier> fieldModifiers();

}
