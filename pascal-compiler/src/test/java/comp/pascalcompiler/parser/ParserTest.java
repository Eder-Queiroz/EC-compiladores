package comp.pascalcompiler.parser;

import comp.pascalcompiler.CompilationException;
import comp.pascalcompiler.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    private void parse(String content) throws CompilationException {
        new Parser(new Lexer(new StringReader(content))).parse();
    }

    private SyntaxException syntaxErrorOf(String content) {
        return assertThrows(SyntaxException.class, () -> parse(content));
    }

    @Test
    void acceptsMinimalProgram() {
        assertDoesNotThrow(() -> parse("program p; begin x := 1; end."));
    }

    @Test
    void acceptsArithmeticExpression() {
        assertDoesNotThrow(() -> parse("program p; begin x := 1 + 2 * (3 - 4) / 5; end."));
    }

    @Test
    void acceptsProcedureCallWithoutArguments() {
        assertDoesNotThrow(() -> parse("program p; begin executa; end."));
    }

    @Test
    void acceptsProcedureCallWithArguments() {
        assertDoesNotThrow(() -> parse("program p; begin mostra(x, y + 1); end."));
    }

    @Test
    void acceptsFunctionCallInsideExpression() {
        assertDoesNotThrow(() -> parse("program p; begin x := dobro(y) + 1; end."));
    }

    @Test
    void acceptsSeveralStatements() {
        assertDoesNotThrow(() -> parse("program p; begin x := 1; y := 2; z := 3; end."));
    }

    @Test
    void rejectsMissingProgramKeyword() {
        SyntaxException exception = syntaxErrorOf("p; begin end.");

        assertEquals("o programa deve começar com 'program' (linha 1, coluna 1)",
                exception.getMessage());
    }

    @Test
    void rejectsMissingProgramName() {
        SyntaxException exception = syntaxErrorOf("program ; begin x := 1; end.");

        assertEquals("faltou o nome do programa (linha 1, coluna 9)", exception.getMessage());
    }

    @Test
    void rejectsMissingSemicolonAfterProgramName() {
        SyntaxException exception = syntaxErrorOf("program p begin x := 1; end.");

        assertEquals("faltou ';' depois do nome do programa (linha 1, coluna 11)",
                exception.getMessage());
    }

    @Test
    void rejectsMissingFinalDot() {
        SyntaxException exception = syntaxErrorOf("program p; begin x := 1; end");

        assertEquals("faltou o '.' no final do programa (linha 1, coluna 29)",
                exception.getMessage());
    }

    @Test
    void rejectsMissingEnd() {
        SyntaxException exception = syntaxErrorOf("program p; begin x := 1;.");

        assertEquals("faltou 'end' (linha 1, coluna 25)", exception.getMessage());
    }

    @Test
    void rejectsMissingSemicolonBetweenStatements() {
        SyntaxException exception = syntaxErrorOf("program p; begin x := 1 y := 2; end.");

        assertEquals("faltou ';' depois do comando (linha 1, coluna 25)", exception.getMessage());
    }

    @Test
    void rejectsUnbalancedParenthesisInExpression() {
        SyntaxException exception = syntaxErrorOf("program p; begin x := (1 + 2; end.");

        assertEquals("faltou ')' na expressão (linha 1, coluna 29)", exception.getMessage());
    }

    @Test
    void rejectsGarbageAfterFinalDot() {
        SyntaxException exception = syntaxErrorOf("program p; begin x := 1; end. sobra");

        assertEquals("esperado o fim do arquivo (linha 1, coluna 31)", exception.getMessage());
    }
}
