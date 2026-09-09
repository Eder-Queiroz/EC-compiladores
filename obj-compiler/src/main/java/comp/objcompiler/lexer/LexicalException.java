package comp.objcompiler.lexer;

import comp.objcompiler.CompilationException;

public class LexicalException extends CompilationException {

    public LexicalException(String message, int line, int column) {
        super(message, line, column);
    }
}
