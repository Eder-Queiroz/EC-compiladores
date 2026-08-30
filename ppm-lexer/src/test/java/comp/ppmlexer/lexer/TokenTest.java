package comp.ppmlexer.lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenTest {

    @Test
    void printsTextValue() {
        Token token = new Token(TokenType.MAGIC, 1, 1, new TokenValue.Text("P3"));

        assertEquals("Token [1, 1, classe=MAGIC, valor=P3]", token.toString());
    }

    @Test
    void printsWholeNumberValue() {
        Token token = new Token(TokenType.NUMERO, 3, 5, new TokenValue.WholeNumber(255));

        assertEquals("Token [3, 5, classe=NUMERO, valor=255]", token.toString());
    }

    @Test
    void omitsValueWhenTokenHasNone() {
        Token token = new Token(TokenType.EOF, 7, 1);

        assertEquals("Token [7, 1, classe=EOF]", token.toString());
    }

    @Test
    void lexicalExceptionCarriesPosition() {
        LexicalException exception = new LexicalException("caractere inválido '@'", 4, 2);

        assertEquals("caractere inválido '@' (linha 4, coluna 2)", exception.getMessage());
        assertEquals(4, exception.line());
        assertEquals(2, exception.column());
    }
}
