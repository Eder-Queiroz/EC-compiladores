package comp.pascalcompiler.parser;

import comp.pascalcompiler.CompilationException;

public class SyntaxException extends CompilationException {

    public SyntaxException(String message, int line, int column) {
        super(message, line, column);
    }
}
