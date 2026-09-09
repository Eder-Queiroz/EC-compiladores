package comp.ppmcompiler.parser;

import comp.ppmcompiler.CompilationException;

public class SyntaxException extends CompilationException {

    public SyntaxException(String message, int line, int column) {
        super(message, line, column);
    }
}
