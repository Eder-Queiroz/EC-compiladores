package comp.ppmcompiler.lexer;

import comp.ppmcompiler.CompilationException;

public class LexicalException extends CompilationException {

    public LexicalException(String message, int line, int column) {
        super(message, line, column);
    }
}
