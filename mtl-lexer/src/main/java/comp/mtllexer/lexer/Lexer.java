package comp.mtllexer.lexer;

import java.io.Reader;
import java.util.Map;

public final class Lexer {

    private static final char COMMENT_MARK = '#';

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "newmtl", TokenType.KW_NEWMTL,
            "Ka", TokenType.KW_KA,
            "Kd", TokenType.KW_KD,
            "Ks", TokenType.KW_KS,
            "Ns", TokenType.KW_NS,
            "illum", TokenType.KW_ILLUM,
            "map_Kd", TokenType.KW_MAP_KD);

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
        if (isIdentifierStart(reader.current())) {
            return readWord(line, column);
        }
        if (Character.isDigit(reader.current())) {
            return readNumber(line, column);
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

    private Token readWord(int line, int column) {
        String lexeme = readWhile(Lexer::isIdentifierPart);
        TokenType type = KEYWORDS.getOrDefault(lexeme, TokenType.IDENTIFICADOR);
        return new Token(type, line, column, new TokenValue.Text(lexeme));
    }

    private Token readNumber(int line, int column) throws LexicalException {
        String integerPart = readWhile(Character::isDigit);
        if (reader.isAtEnd() || reader.current() != '.') {
            return wholeNumber(integerPart, line, column);
        }
        reader.advance();
        String lexeme = integerPart + "." + readWhile(Character::isDigit);
        try {
            return new Token(TokenType.FLOAT, line, column,
                    new TokenValue.DecimalNumber(Double.parseDouble(lexeme)));
        } catch (NumberFormatException exception) {
            throw new LexicalException("número mal formado '" + lexeme + "'", line, column);
        }
    }

    private Token wholeNumber(String lexeme, int line, int column) throws LexicalException {
        try {
            return new Token(TokenType.INTEIRO, line, column, new TokenValue.WholeNumber(Integer.parseInt(lexeme)));
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

    private static boolean isIdentifierStart(char character) {
        return Character.isLetter(character) || character == '_';
    }

    private static boolean isIdentifierPart(char character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == '-' || character == '.';
    }

    private interface CharacterPredicate {
        boolean test(char character);
    }
}
