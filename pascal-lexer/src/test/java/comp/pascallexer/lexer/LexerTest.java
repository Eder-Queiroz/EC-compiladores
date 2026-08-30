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

    @Test
    void recognizesTwoCharacterOperatorsBeforeSingleOnes() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=ASSIGN, valor=:=]",
                        "Token [1, 4, classe=LESS_EQUAL, valor=<=]",
                        "Token [1, 7, classe=NOT_EQUAL, valor=<>]",
                        "Token [1, 10, classe=GREATER_EQUAL, valor=>=]",
                        "Token [1, 12, classe=EOF]"),
                printedTokensOf(":= <= <> >="));
    }

    @Test
    void recognizesSingleCharacterOperators() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=COLON, valor=:]",
                        "Token [1, 3, classe=LESS, valor=<]",
                        "Token [1, 5, classe=GREATER, valor=>]",
                        "Token [1, 7, classe=EQUAL, valor==]",
                        "Token [1, 9, classe=PLUS, valor=+]",
                        "Token [1, 11, classe=MINUS, valor=-]",
                        "Token [1, 13, classe=TIMES, valor=*]",
                        "Token [1, 15, classe=DIVIDE, valor=/]",
                        "Token [1, 16, classe=EOF]"),
                printedTokensOf(": < > = + - * /"));
    }

    @Test
    void recognizesDelimiters() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=SEMICOLON, valor=;]",
                        "Token [1, 3, classe=COMMA, valor=,]",
                        "Token [1, 5, classe=DOT, valor=.]",
                        "Token [1, 7, classe=LEFT_PAREN, valor=(]",
                        "Token [1, 9, classe=RIGHT_PAREN, valor=)]",
                        "Token [1, 10, classe=EOF]"),
                printedTokensOf("; , . ( )"));
    }

    @Test
    void recognizesStringLiteral() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=WRITE, valor=write]",
                        "Token [1, 6, classe=LEFT_PAREN, valor=(]",
                        "Token [1, 7, classe=STRING, valor=Resultado: ]",
                        "Token [1, 20, classe=RIGHT_PAREN, valor=)]",
                        "Token [1, 21, classe=EOF]"),
                printedTokensOf("write('Resultado: ')"));
    }

    @Test
    void unescapesDoubledQuoteInsideString() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=STRING, valor=nao e'facil]", "Token [1, 15, classe=EOF]"),
                printedTokensOf("'nao e''facil'"));
    }

    @Test
    void rejectsUnterminatedString() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("'aberta\n"));

        assertEquals("cadeia não terminada (linha 1, coluna 1)", exception.getMessage());
    }

    @Test
    void ignoresBraceComment() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=VAR, valor=var]",
                        "Token [1, 18, classe=ID, valor=x]",
                        "Token [1, 19, classe=EOF]"),
                printedTokensOf("var {comentario} x"));
    }

    @Test
    void ignoresBraceCommentSpanningLines() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=VAR, valor=var]",
                        "Token [2, 10, classe=ID, valor=x]",
                        "Token [2, 11, classe=EOF]"),
                printedTokensOf("var { linha1\nlinha2 } x"));
    }

    @Test
    void ignoresParenthesisComment() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=VAR, valor=var]",
                        "Token [1, 22, classe=ID, valor=x]",
                        "Token [1, 23, classe=EOF]"),
                printedTokensOf("var (* comentário *) x"));
    }

    @Test
    void distinguishesOpeningParenthesisFromComment() throws LexicalException {
        assertEquals(
                List.of("Token [1, 1, classe=LEFT_PAREN, valor=(]",
                        "Token [1, 2, classe=ID, valor=a]",
                        "Token [1, 3, classe=RIGHT_PAREN, valor=)]",
                        "Token [1, 4, classe=EOF]"),
                printedTokensOf("(a)"));
    }

    @Test
    void rejectsUnterminatedBraceComment() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("var { sem fim"));

        assertEquals("comentário não terminado (linha 1, coluna 5)", exception.getMessage());
    }

    @Test
    void rejectsUnterminatedParenthesisComment() {
        LexicalException exception = assertThrows(LexicalException.class, () -> tokensOf("var (* sem fim"));

        assertEquals("comentário não terminado (linha 1, coluna 5)", exception.getMessage());
    }

    @Test
    void scansCompleteProgram() throws LexicalException {
        String content = """
                program exemplo;
                var
                  x: integer;
                begin
                  read(x);
                  if (x >= 10) then
                  begin
                    writeln('grande');
                  end;
                end.
                """;

        List<Token> tokens = tokensOf(content);

        assertEquals(TokenType.PROGRAM, tokens.get(0).type());
        assertEquals("Token [3, 4, classe=COLON, valor=:]", tokens.get(5).toString());
        assertEquals(TokenType.DOT, tokens.get(tokens.size() - 2).type());
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).type());
    }
}
