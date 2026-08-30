package comp.pascallexer.lexer;

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
    void recognizesReservedWords() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=PROGRAM, valor=program]",
                        "Token [1, 9, classe=VAR, valor=var]",
                        "Token [1, 13, classe=BEGIN, valor=begin]",
                        "Token [1, 19, classe=END, valor=end]",
                        "Token [1, 22, classe=EOF]"),
                printedTokensOf("program var begin end"));
    }

    @Test
    void treatsReservedWordsAsCaseInsensitive() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=BEGIN, valor=Begin]",
                        "Token [1, 7, classe=IF, valor=IF]",
                        "Token [1, 9, classe=EOF]"),
                printedTokensOf("Begin IF"));
    }

    @Test
    void recognizesIdentifiersAndIntegers() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=ID, valor=contador1]",
                        "Token [1, 11, classe=INTNUM, valor=42]",
                        "Token [1, 13, classe=EOF]"),
                printedTokensOf("contador1 42"));
    }

    @Test
    void identifierCannotStartWithDigit() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=INTNUM, valor=1]",
                        "Token [1, 2, classe=ID, valor=a]",
                        "Token [1, 3, classe=EOF]"),
                printedTokensOf("1a"));
    }

    @Test
    void reportsEofForEmptyInput() throws LexicalException {
        assertEquals(List.of("Token [1, 1, classe=EOF]"), printedTokensOf(""));
    }

    @Test
    void rejectsInvalidCharacter() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("var\n@"));

        assertEquals("caractere inválido '@' (linha 2, coluna 1)", exception.getMessage());
    }
}
