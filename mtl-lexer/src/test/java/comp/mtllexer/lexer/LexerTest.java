package comp.mtllexer.lexer;

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
    void recognizesKeywords() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_NEWMTL, valor=newmtl]",
                        "Token [1, 8, classe=KW_KA, valor=Ka]",
                        "Token [1, 11, classe=KW_KD, valor=Kd]",
                        "Token [1, 14, classe=KW_KS, valor=Ks]",
                        "Token [1, 17, classe=KW_NS, valor=Ns]",
                        "Token [1, 20, classe=KW_ILLUM, valor=illum]",
                        "Token [1, 26, classe=KW_MAP_KD, valor=map_Kd]",
                        "Token [1, 32, classe=EOF]"),
                printedTokensOf("newmtl Ka Kd Ks Ns illum map_Kd"));
    }

    @Test
    void treatsKeywordsAsCaseSensitive() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=IDENTIFICADOR, valor=kd]", "Token [1, 3, classe=EOF]"),
                printedTokensOf("kd"));
    }

    @Test
    void distinguishesIntegerFromFloat() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=INTEIRO, valor=2]",
                        "Token [1, 3, classe=FLOAT, valor=10.0]",
                        "Token [1, 8, classe=FLOAT, valor=0.5]",
                        "Token [1, 11, classe=EOF]"),
                printedTokensOf("2 10.0 0.5"));
    }

    @Test
    void readsFileNameAsSingleIdentifier() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_MAP_KD, valor=map_Kd]",
                        "Token [1, 8, classe=IDENTIFICADOR, valor=texture.ppm]",
                        "Token [1, 19, classe=EOF]"),
                printedTokensOf("map_Kd texture.ppm"));
    }

    @Test
    void readsMaterialNameAsIdentifier() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_NEWMTL, valor=newmtl]",
                        "Token [1, 8, classe=IDENTIFICADOR, valor=MatMadeira]",
                        "Token [1, 18, classe=EOF]"),
                printedTokensOf("newmtl MatMadeira"));
    }

    @Test
    void scansCompleteMaterial() throws LexicalException {
        String content = """
                newmtl texture
                Ka 0.0 0.0 0.0
                illum 2
                map_Kd texture.ppm
                """;

        List<Token> tokens = tokensOf(content);

        assertEquals(11, tokens.size());
        assertEquals("Token [1, 8, classe=IDENTIFICADOR, valor=texture]", tokens.get(1).toString());
        assertEquals("Token [2, 4, classe=FLOAT, valor=0.0]", tokens.get(3).toString());
        assertEquals("Token [3, 7, classe=INTEIRO, valor=2]", tokens.get(7).toString());
        assertEquals("Token [5, 1, classe=EOF]", tokens.get(10).toString());
    }

    @Test
    void ignoresComments() throws LexicalException {
        assertEquals(
                List.of("Token [2, 1, classe=KW_NS, valor=Ns]",
                        "Token [2, 4, classe=FLOAT, valor=10.0]",
                        "Token [2, 8, classe=EOF]"),
                printedTokensOf("# material\nNs 10.0"));
    }

    @Test
    void reportsEofForEmptyInput() throws LexicalException {
        assertEquals(List.of("Token [1, 1, classe=EOF]"), printedTokensOf(""));
    }

    @Test
    void rejectsInvalidCharacter() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("Ka 0.0\n$"));

        assertEquals("caractere inválido '$' (linha 2, coluna 1)", exception.getMessage());
    }
}
