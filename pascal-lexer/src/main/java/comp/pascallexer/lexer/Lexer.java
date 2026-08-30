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

    private static final Map<Character, TokenType> SINGLE_CHARACTER_SYMBOLS = Map.ofEntries(
            Map.entry('=', TokenType.EQUAL),
            Map.entry('+', TokenType.PLUS),
            Map.entry('-', TokenType.MINUS),
            Map.entry('*', TokenType.TIMES),
            Map.entry('/', TokenType.DIVIDE),
            Map.entry(';', TokenType.SEMICOLON),
            Map.entry(',', TokenType.COMMA),
            Map.entry('.', TokenType.DOT),
            Map.entry(':', TokenType.COLON),
            Map.entry('<', TokenType.LESS),
            Map.entry('>', TokenType.GREATER),
            Map.entry(')', TokenType.RIGHT_PAREN));

    private static final char BRACE_COMMENT_START = '{';
    private static final char BRACE_COMMENT_END = '}';
    private static final char PARENTHESIS_COMMENT_MARK = '*';
    private static final char QUOTE = '\'';

    private final SourceReader reader;

    public Lexer(Reader source) {
        this.reader = new SourceReader(source);
    }

    public Token nextToken() throws LexicalException {
        while (true) {
            skipBlanks();

            int line = reader.line();
            int column = reader.column();

            if (reader.isAtEnd()) {
                return new Token(TokenType.EOF, line, column);
            }
            if (reader.current() == BRACE_COMMENT_START) {
                skipBraceComment(line, column);
                continue;
            }
            if (reader.current() == '(') {
                reader.advance();
                if (!reader.isAtEnd() && reader.current() == PARENTHESIS_COMMENT_MARK) {
                    skipParenthesisComment(line, column);
                    continue;
                }
                return symbol(TokenType.LEFT_PAREN, "(", line, column);
            }
            if (Character.isLetter(reader.current())) {
                return readWord(line, column);
            }
            if (Character.isDigit(reader.current())) {
                return readInteger(line, column);
            }
            if (reader.current() == QUOTE) {
                return readString(line, column);
            }
            return readSymbol(line, column);
        }
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

    private void skipBraceComment(int line, int column) throws LexicalException {
        reader.advance();
        while (!reader.isAtEnd() && reader.current() != BRACE_COMMENT_END) {
            reader.advance();
        }
        if (reader.isAtEnd()) {
            throw new LexicalException("comentário não terminado", line, column);
        }
        reader.advance();
    }

    private void skipParenthesisComment(int line, int column) throws LexicalException {
        reader.advance();
        while (!reader.isAtEnd()) {
            if (reader.current() != PARENTHESIS_COMMENT_MARK) {
                reader.advance();
                continue;
            }
            reader.advance();
            if (!reader.isAtEnd() && reader.current() == ')') {
                reader.advance();
                return;
            }
        }
        throw new LexicalException("comentário não terminado", line, column);
    }

    private Token readString(int line, int column) throws LexicalException {
        reader.advance();
        StringBuilder text = new StringBuilder();
        while (true) {
            if (reader.isAtEnd() || reader.current() == '\n') {
                throw new LexicalException("cadeia não terminada", line, column);
            }
            if (reader.current() == QUOTE) {
                reader.advance();
                if (reader.isAtEnd() || reader.current() != QUOTE) {
                    return new Token(TokenType.STRING, line, column, new TokenValue.Text(text.toString()));
                }
            }
            text.append(reader.current());
            reader.advance();
        }
    }

    private Token readSymbol(int line, int column) throws LexicalException {
        char first = reader.current();
        reader.advance();

        if (first == ':' && matches('=')) {
            return symbol(TokenType.ASSIGN, ":=", line, column);
        }
        if (first == '<' && matches('=')) {
            return symbol(TokenType.LESS_EQUAL, "<=", line, column);
        }
        if (first == '<' && matches('>')) {
            return symbol(TokenType.NOT_EQUAL, "<>", line, column);
        }
        if (first == '>' && matches('=')) {
            return symbol(TokenType.GREATER_EQUAL, ">=", line, column);
        }
        TokenType type = SINGLE_CHARACTER_SYMBOLS.get(first);
        if (type == null) {
            throw new LexicalException("caractere inválido '" + first + "'", line, column);
        }
        return symbol(type, String.valueOf(first), line, column);
    }

    private boolean matches(char expected) {
        if (reader.isAtEnd() || reader.current() != expected) {
            return false;
        }
        reader.advance();
        return true;
    }

    private Token symbol(TokenType type, String lexeme, int line, int column) {
        return new Token(type, line, column, new TokenValue.Text(lexeme));
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
