package comp.mtllexer.lexer;

public class LexicalException extends Exception {

    private final int line;
    private final int column;

    public LexicalException(String message, int line, int column) {
        super(message + " (linha " + line + ", coluna " + column + ")");
        this.line = line;
        this.column = column;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}
