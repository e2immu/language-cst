package org.e2immu.language.cst.impl.expression.eval;

import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.runtime.Runtime;
import org.e2immu.language.cst.api.type.ParameterizedType;
import org.e2immu.util.internal.util.IntUtil;

public class EvalCast {
    private final Runtime runtime;

    public EvalCast(Runtime runtime) {
        this.runtime = runtime;
    }

    public Expression eval(Expression e, Cast cast) {
        ParameterizedType castType = cast.parameterizedType();

        if (castType.isAssignableFrom(runtime, e.parameterizedType())) return e;
        if (!(e instanceof ConstantExpression<?>)) return cast;
        Boolean b = redundantIntegerCast(e, castType);
        if (b != null) return b ? e : cast;
        Boolean c = redundantDoubleCast(e, castType);
        if (c != null) return c ? e : cast;
        return cast;
    }

    public static Boolean redundantDoubleCast(Expression e, ParameterizedType castType) {
        double d;
        if (e instanceof FloatConstant fc) d = fc.doubleValue();
        else if (e instanceof DoubleConstant dc) d = dc.doubleValue();
        else return null;
        return castType.isFloat() && (float) d == d || castType.isDouble();
    }

    public static Boolean redundantIntegerCast(Expression e, ParameterizedType castType) {
        long l;
        if (e instanceof ByteConstant bc) l = bc.constant();
        else if (e instanceof ShortConstant sc) l = sc.constant();
        else if (e instanceof IntConstant ic) l = ic.constant();
        else if (e instanceof LongConstant lc) l = lc.constant();
        else if (e instanceof DoubleConstant dc && IntUtil.isMathematicalInteger(dc.constant()))
            l = (long) dc.doubleValue();
        else if (e instanceof FloatConstant fc && IntUtil.isMathematicalInteger(fc.doubleValue()))
            l = (long) fc.doubleValue();
        else return null;
        return castType.isByte() && l <= Byte.MAX_VALUE && l >= Byte.MIN_VALUE
               || castType.isShort() && l <= Short.MAX_VALUE && l >= Short.MIN_VALUE
               || castType.isInt() && l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE
               || castType.isLong();
    }
}
