package comp.objcompiler.parser;

import comp.objcompiler.CompilationException;

public class SyntaxException extends CompilationException {

    public SyntaxException(String message, int line, int column) {
        super(message, line, column);
    }
}
