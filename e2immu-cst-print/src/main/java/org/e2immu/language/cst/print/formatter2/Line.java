package org.e2immu.language.cst.print.formatter2;

class Line {
    final int maxAvailable;
    final StringBuilder stringBuilder = new StringBuilder();

    private int available;

    Line(int maxAvailable) {
        this.maxAvailable = maxAvailable;
        available = maxAvailable;
    }

    public void appendBeforeSplit(String string) {
        stringBuilder.append(string);
    }

    public void computeAvailable() {
        int lastNewLine = stringBuilder.lastIndexOf("\n");
        if (lastNewLine < 0) {
            available -= stringBuilder.length();
        } else {
            int remainder = stringBuilder.length() - (lastNewLine + 1);
            available = maxAvailable - remainder;
        }
    }

    public void appendNoNewLine(String string) {
        assert !string.contains("\n");
        stringBuilder.append(string);
        available -= string.length();
    }

    public int available() {
        return available;
    }

    public char lastCharacter() {
        return stringBuilder.charAt(stringBuilder.length() - 1);
    }

    public int length() {
        return stringBuilder.length();
    }

    public void newAvailable(int writtenAfterNewline) {
        available = maxAvailable - writtenAfterNewline;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    boolean isNotEmptyDoesNotEndInNewLine() {
        return !stringBuilder.isEmpty() && '\n' != stringBuilder.charAt(stringBuilder.length() - 1);
    }

    public void trim() {
        while (!stringBuilder.isEmpty() && stringBuilder.charAt(stringBuilder.length() - 1) == ' ') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            available++;
        }
    }

    /*
    return the number of characters added FIXME needs more work
     */
    public int carryOutSplit(int pos, int indent, boolean doubleSplit) {
        String insert = (doubleSplit ? "\n\n" : "\n") + (" ".repeat(indent));
        int pos2 = bestSplit(stringBuilder, pos);
        char atPos = stringBuilder.charAt(pos2);
        if (atPos == ' ') {
            // split is ' \n', and we'll replace at the space by '\n' rather than '\n   '
            if (pos2 < stringBuilder.length() - 1 && stringBuilder.charAt(pos2 + 1) == '\n') {
                stringBuilder.replace(pos2, pos2 + 1, doubleSplit ? "\n\n" : "\n");
            } else {
                stringBuilder.replace(pos2, pos2 + 1, insert);
            }
            return indent;
        } else if (atPos != '\n') {
            stringBuilder.insert(pos2, insert);
            return indent + 1;
        }
        // we already have a newline here
        // IGNORE, this may help with the double splits that are missing for comments
        return 0;
    }

    /*
    'xyz,', pos == 3, then pos stays the same, we'll split on the comma
    'xyz {', pos == 4, then we'll split on the space, even though the FIXME why?
     */
    private static int bestSplit(StringBuilder sb, int pos) {
        while (pos >= 1 && Character.isWhitespace(sb.charAt(pos - 1))) --pos;
        return pos;
    }
}
