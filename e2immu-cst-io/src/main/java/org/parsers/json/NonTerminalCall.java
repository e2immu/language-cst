/* Generated by: CongoCC Parser Generator. NonTerminalCall.java */
package org.parsers.json;

import java.io.PrintStream;


public class NonTerminalCall {
    final TokenSource lexer;
    final String sourceFile;
    public final String productionName;
    final String parserClassName;
    final int line, column;

    public NonTerminalCall(String parserClassName, TokenSource lexer, String sourceFile, String productionName, int line, int column) {
        this.parserClassName = parserClassName;
        this.lexer = lexer;
        this.sourceFile = sourceFile;
        this.productionName = productionName;
        this.line = line;
        this.column = column;
    }

    final TokenSource getTokenSource() {
        return lexer;
    }

    StackTraceElement createStackTraceElement() {
        return new StackTraceElement("JSONParser", productionName, sourceFile, line);
    }

    public void dump(PrintStream ps) {
        ps.println(productionName + ":" + line + ":" + column);
    }

}


