package comp.pascallexer.lexer;

import java.io.Reader;
import java.util.Locale;
import java.util.Map;

public final class Lexer {

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("program", TokenType.PROGRAM),
            Map.entry("var", TokenType.VAR),
            Map.entry("integer", TokenType.INTEGER),
            Map.entry("procedure", TokenType.PROCEDURE),
            Map.entry("function", TokenType.FUNCTION),
            Map.entry("begin", TokenType.BEGIN),
            Map.entry("end", TokenType.END),
            Map.entry("read", TokenType.READ),
            Map.entry("write", TokenType.WRITE),
            Map.entry("writeln", TokenType.WRITELN),
            Map.entry("for", TokenType.FOR),
            Map.entry("to", TokenType.TO),
            Map.entry("do", TokenType.DO),
            Map.entry("repeat", TokenType.REPEAT),
            Map.entry("until", TokenType.UNTIL),
            Map.entry("while", TokenType.WHILE),
            Map.entry("if", TokenType.IF),
            Map.entry("then", TokenType.THEN),
            Map.entry("else", TokenType.ELSE),
            Map.entry("or", TokenType.OR),
            Map.entry("and", TokenType.AND),
            Map.entry("not", TokenType.NOT),
            Map.entry("true", TokenType.TRUE),
            Map.entry("false", TokenType.FALSE));

    private final SourceReader reader;

    public Lexer(Reader source) {
        this.reader = new SourceReader(source);
    }

    public Token nextToken() throws LexicalException {
        skipBlanks();

        int line = reader.line();
        int column = reader.column();

        if (reader.isAtEnd()) {
            return new Token(TokenType.EOF, line, column);
        }
        if (Character.isLetter(reader.current())) {
            return readWord(line, column);
        }
        if (Character.isDigit(reader.current())) {
            return readInteger(line, column);
        }
        throw new LexicalException("caractere inválido '" + reader.current() + "'", line, column);
    }

    private void skipBlanks() {
        while (!reader.isAtEnd() && Character.isWhitespace(reader.current())) {
            reader.advance();
        }
    }

    private Token readWord(int line, int column) {
        String lexeme = readWhile(Character::isLetterOrDigit);
        TokenType type = KEYWORDS.getOrDefault(lexeme.toLowerCase(Locale.ROOT), TokenType.ID);
        return new Token(type, line, column, new TokenValue.Text(lexeme));
    }

    private Token readInteger(int line, int column) throws LexicalException {
        String lexeme = readWhile(Character::isDigit);
        try {
            return new Token(TokenType.INTNUM, line, column, new TokenValue.WholeNumber(Integer.parseInt(lexeme)));
        } catch (NumberFormatException exception) {
            throw new LexicalException("número fora do intervalo '" + lexeme + "'", line, column);
        }
    }

    private String readWhile(CharacterPredicate accepted) {
        StringBuilder lexeme = new StringBuilder();
        while (!reader.isAtEnd() && accepted.test(reader.current())) {
            lexeme.append(reader.current());
            reader.advance();
        }
        return lexeme.toString();
    }

    private interface CharacterPredicate {
        boolean test(char character);
    }
}
