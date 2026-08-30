package comp.ppmlexer.lexer;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LexerTest {

    private List<Token> tokensOf(String content) throws LexicalException {
        Lexer lexer = new Lexer(new StringReader(content));
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = lexer.nextToken();
            tokens.add(token);
        } while (token.type() != TokenType.EOF);
        return tokens;
    }

    private List<String> printedTokensOf(String content) throws LexicalException {
        return tokensOf(content).stream().map(Token::toString).toList();
    }

    @Test
    void recognizesMagicNumber() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=MAGIC, valor=P3]", "Token [1, 3, classe=EOF]"),
                printedTokensOf("P3"));
    }

    @Test
    void recognizesNumbers() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=NUMERO, valor=3]",
                        "Token [1, 3, classe=NUMERO, valor=2]",
                        "Token [1, 5, classe=NUMERO, valor=255]",
                        "Token [1, 8, classe=EOF]"),
                printedTokensOf("3 2 255"));
    }

    @Test
    void scansCompleteImageHeaderAndPixels() throws LexicalException {
        String content = """
                P3
                3 2
                255
                255 0 0
                """;

        List<Token> tokens = tokensOf(content);

        assertEquals(8, tokens.size());
        assertEquals("Token [1, 1, classe=MAGIC, valor=P3]", tokens.get(0).toString());
        assertEquals("Token [2, 1, classe=NUMERO, valor=3]", tokens.get(1).toString());
        assertEquals("Token [3, 1, classe=NUMERO, valor=255]", tokens.get(3).toString());
        assertEquals("Token [4, 5, classe=NUMERO, valor=0]", tokens.get(5).toString());
        assertEquals("Token [5, 1, classe=EOF]", tokens.get(7).toString());
    }

    @Test
    void ignoresCommentsUntilEndOfLine() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=MAGIC, valor=P3]",
                        "Token [3, 1, classe=NUMERO, valor=3]",
                        "Token [3, 2, classe=EOF]"),
                printedTokensOf("P3\n# comentário 999\n3"));
    }

    @Test
    void ignoresCommentAtEndOfLineWithContent() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=NUMERO, valor=255]", "Token [1, 17, classe=EOF]"),
                printedTokensOf("255 # cor máxima"));
    }

    @Test
    void reportsEofForEmptyInput() throws LexicalException {
        assertEquals(List.of("Token [1, 1, classe=EOF]"), printedTokensOf(""));
    }

    @Test
    void rejectsInvalidCharacter() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("P3\n@"));

        assertEquals(2, exception.line());
        assertEquals(1, exception.column());
        assertEquals("caractere inválido '@' (linha 2, coluna 1)", exception.getMessage());
    }

    @Test
    void rejectsUnknownMagicNumber() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("P6 3 2"));

        assertEquals("número mágico inválido 'P6' (linha 1, coluna 1)", exception.getMessage());
    }
}
