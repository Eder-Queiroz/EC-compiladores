package comp.mtlcompiler.lexer;

import comp.mtlcompiler.CompilationException;

public class LexicalException extends CompilationException {

    public LexicalException(String message, int line, int column) {
        super(message, line, column);
    }
}
