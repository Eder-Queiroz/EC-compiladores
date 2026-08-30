package comp.pascallexer.lexer;

public record Token(TokenType type, int line, int column, TokenValue value) {

    public Token(TokenType type, int line, int column) {
        this(type, line, column, null);
    }

    @Override
    public String toString() {
        String header = "Token [" + line + ", " + column + ", classe=" + type;
        return value == null ? header + "]" : header + ", valor=" + value + "]";
    }
}
