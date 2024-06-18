package org.e2immu.cstimpl.util;

public class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException();
    }

    public static void indent(StringBuilder sb, int num) {
        sb.append(" ".repeat(Math.max(0, num)));
    }

    public static String quote(String s) {
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

}
