package comp.objlexer.lexer;

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
    void appliesMaximalMunchToVertexKeywords() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_V, valor=v]",
                        "Token [1, 3, classe=KW_VT, valor=vt]",
                        "Token [1, 6, classe=KW_VN, valor=vn]",
                        "Token [1, 8, classe=EOF]"),
                printedTokensOf("v vt vn"));
    }

    @Test
    void recognizesRemainingKeywords() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_MTLLIB, valor=mtllib]",
                        "Token [1, 8, classe=KW_USEMTL, valor=usemtl]",
                        "Token [1, 15, classe=KW_F, valor=f]",
                        "Token [1, 17, classe=KW_G, valor=g]",
                        "Token [1, 19, classe=KW_O, valor=o]",
                        "Token [1, 20, classe=EOF]"),
                printedTokensOf("mtllib usemtl f g o"));
    }

    @Test
    void recognizesNegativeFloat() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_VN, valor=vn]",
                        "Token [1, 4, classe=FLOAT, valor=-1.0]",
                        "Token [1, 9, classe=FLOAT, valor=0.0]",
                        "Token [1, 12, classe=EOF]"),
                printedTokensOf("vn -1.0 0.0"));
    }

    @Test
    void distinguishesIntegerFromFloat() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_VT, valor=vt]",
                        "Token [1, 4, classe=FLOAT, valor=0.25]",
                        "Token [1, 9, classe=INTEIRO, valor=0]",
                        "Token [1, 10, classe=EOF]"),
                printedTokensOf("vt 0.25 0"));
    }

    @Test
    void splitsFaceIndicesOnSlash() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_F, valor=f]",
                        "Token [1, 3, classe=INTEIRO, valor=3]",
                        "Token [1, 4, classe=BARRA, valor=/]",
                        "Token [1, 5, classe=INTEIRO, valor=10]",
                        "Token [1, 7, classe=BARRA, valor=/]",
                        "Token [1, 8, classe=INTEIRO, valor=1]",
                        "Token [1, 9, classe=EOF]"),
                printedTokensOf("f 3/10/1"));
    }

    @Test
    void readsFileNameAsSingleIdentifier() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_MTLLIB, valor=mtllib]",
                        "Token [1, 8, classe=IDENTIFICADOR, valor=cube.mtl]",
                        "Token [1, 16, classe=EOF]"),
                printedTokensOf("mtllib cube.mtl"));
    }

    @Test
    void ignoresCommentAtEndOfLineWithContent() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=KW_V, valor=v]",
                        "Token [1, 3, classe=FLOAT, valor=0.0]",
                        "Token [2, 1, classe=KW_G, valor=g]",
                        "Token [2, 3, classe=IDENTIFICADOR, valor=cube]",
                        "Token [2, 7, classe=EOF]"),
                printedTokensOf("v 0.0  # 1 a\ng cube"));
    }

    @Test
    void reportsEofForEmptyInput() throws LexicalException {
        assertEquals(List.of("Token [1, 1, classe=EOF]"), printedTokensOf(""));
    }

    @Test
    void rejectsInvalidCharacter() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("v 1.0\n%"));

        assertEquals("caractere inválido '%' (linha 2, coluna 1)", exception.getMessage());
    }

    @Test
    void rejectsLonelyMinusSign() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("vn -"));

        assertEquals("número mal formado '-' (linha 1, coluna 4)", exception.getMessage());
    }

    @Test
    void rejectsLonelyDot() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("vt ."));

        assertEquals("número mal formado '.' (linha 1, coluna 4)", exception.getMessage());
    }
}
