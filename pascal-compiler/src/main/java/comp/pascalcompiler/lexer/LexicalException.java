package comp.pascalcompiler.lexer;

import comp.pascalcompiler.CompilationException;

public class LexicalException extends CompilationException {

    public LexicalException(String message, int line, int column) {
        super(message, line, column);
    }
}
