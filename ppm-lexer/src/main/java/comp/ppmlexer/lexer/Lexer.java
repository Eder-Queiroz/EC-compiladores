package comp.ppmlexer.lexer;

import java.io.Reader;

public final class Lexer {

    private static final String MAGIC_NUMBER = "P3";
    private static final char COMMENT_MARK = '#';

    private final SourceReader reader;

    public Lexer(Reader source) {
        this.reader = new SourceReader(source);
    }

    public Token nextToken() throws LexicalException {
        skipBlanksAndComments();

        int line = reader.line();
        int column = reader.column();

        if (reader.isAtEnd()) {
            return new Token(TokenType.EOF, line, column);
        }
        if (Character.isDigit(reader.current())) {
            return readNumber(line, column);
        }
        if (reader.current() == MAGIC_NUMBER.charAt(0)) {
            return readMagicNumber(line, column);
        }
        throw new LexicalException("caractere inválido '" + reader.current() + "'", line, column);
    }

    private void skipBlanksAndComments() {
        while (!reader.isAtEnd()) {
            if (Character.isWhitespace(reader.current())) {
                reader.advance();
            } else if (reader.current() == COMMENT_MARK) {
                skipComment();
            } else {
                return;
            }
        }
    }

    private void skipComment() {
        while (!reader.isAtEnd() && reader.current() != '\n') {
            reader.advance();
        }
    }

    private Token readNumber(int line, int column) throws LexicalException {
        String lexeme = readWhile(Character::isDigit);
        try {
            return new Token(TokenType.NUMERO, line, column, new TokenValue.WholeNumber(Integer.parseInt(lexeme)));
        } catch (NumberFormatException exception) {
            throw new LexicalException("número fora do intervalo '" + lexeme + "'", line, column);
        }
    }

    private Token readMagicNumber(int line, int column) throws LexicalException {
        String lexeme = readWhile(character -> !Character.isWhitespace(character));
        if (!MAGIC_NUMBER.equals(lexeme)) {
            throw new LexicalException("número mágico inválido '" + lexeme + "'", line, column);
        }
        return new Token(TokenType.MAGIC, line, column, new TokenValue.Text(lexeme));
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
