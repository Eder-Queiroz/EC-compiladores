package comp.objlexer.lexer;

import java.io.Reader;
import java.util.Map;

public final class Lexer {

    private static final char COMMENT_MARK = '#';
    private static final char SLASH = '/';

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "mtllib", TokenType.KW_MTLLIB,
            "usemtl", TokenType.KW_USEMTL,
            "v", TokenType.KW_V,
            "vt", TokenType.KW_VT,
            "vn", TokenType.KW_VN,
            "f", TokenType.KW_F,
            "g", TokenType.KW_G,
            "o", TokenType.KW_O);

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
        if (isNumberStart(reader.current())) {
            return readNumber(line, column);
        }
        if (reader.current() == SLASH) {
            reader.advance();
            return new Token(TokenType.BARRA, line, column, new TokenValue.Text(String.valueOf(SLASH)));
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
        StringBuilder lexeme = new StringBuilder();
        if (reader.current() == '-') {
            lexeme.append(reader.current());
            reader.advance();
        }
        lexeme.append(readWhile(Character::isDigit));
        if (reader.isAtEnd() || reader.current() != '.') {
            return wholeNumber(lexeme.toString(), line, column);
        }
        reader.advance();
        lexeme.append('.').append(readWhile(Character::isDigit));
        return decimalNumber(lexeme.toString(), line, column);
    }

    private Token wholeNumber(String lexeme, int line, int column) throws LexicalException {
        try {
            return new Token(TokenType.INTEIRO, line, column, new TokenValue.WholeNumber(Integer.parseInt(lexeme)));
        } catch (NumberFormatException exception) {
            throw new LexicalException("número mal formado '" + lexeme + "'", line, column);
        }
    }

    private Token decimalNumber(String lexeme, int line, int column) throws LexicalException {
        try {
            return new Token(TokenType.FLOAT, line, column,
                    new TokenValue.DecimalNumber(Double.parseDouble(lexeme)));
        } catch (NumberFormatException exception) {
            throw new LexicalException("número mal formado '" + lexeme + "'", line, column);
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

    private static boolean isNumberStart(char character) {
        return Character.isDigit(character) || character == '-' || character == '.';
    }

    private interface CharacterPredicate {
        boolean test(char character);
    }
}
