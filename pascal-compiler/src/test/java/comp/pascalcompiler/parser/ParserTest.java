package comp.pascalcompiler.parser;

import comp.pascalcompiler.CompilationException;
import comp.pascalcompiler.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @Test
    void acceptsSingleVariableDeclaration() {
        assertDoesNotThrow(() -> parse("program p; var x : integer; begin x := 1; end."));
    }

    @Test
    void acceptsSeveralVariablesInOneDeclaration() {
        assertDoesNotThrow(() -> parse("program p; var x, y, z : integer; begin x := 1; end."));
    }

    @Test
    void acceptsSeveralDeclarationLines() {
        assertDoesNotThrow(() -> parse(
                "program p; var x : integer; y, z : integer; begin x := 1; end."));
    }

    @Test
    void acceptsProcedureWithoutParameters() {
        assertDoesNotThrow(() -> parse(
                "program p; procedure mostra; begin x := 1; end; begin mostra; end."));
    }

    @Test
    void acceptsProcedureWithParameters() {
        assertDoesNotThrow(() -> parse(
                "program p; procedure mostra(a, b : integer; c : integer); begin x := a; end; begin mostra(1, 2, 3); end."));
    }

    @Test
    void acceptsFunctionWithParameters() {
        assertDoesNotThrow(() -> parse(
                "program p; function dobro(n : integer) : integer; begin dobro := n * 2; end; begin x := dobro(2); end."));
    }

    @Test
    void acceptsRoutineWithItsOwnDeclarations() {
        assertDoesNotThrow(() -> parse(
                "program p; function dobro(n : integer) : integer; var aux : integer; begin aux := n * 2; end; begin x := dobro(2); end."));
    }

    @Test
    void rejectsUnknownVariableType() {
        SyntaxException exception = syntaxErrorOf("program p; var x : real; begin x := 1; end.");

        assertEquals("o único tipo aceito é 'integer' (linha 1, coluna 20)", exception.getMessage());
    }

    @Test
    void rejectsMissingColonInDeclaration() {
        SyntaxException exception = syntaxErrorOf("program p; var x integer; begin x := 1; end.");

        assertEquals("faltou ':' antes do tipo da variável (linha 1, coluna 18)",
                exception.getMessage());
    }

    @Test
    void rejectsMissingSemicolonAfterDeclaration() {
        SyntaxException exception = syntaxErrorOf("program p; var x : integer begin x := 1; end.");

        assertEquals("faltou ';' depois da declaração de variáveis (linha 1, coluna 28)",
                exception.getMessage());
    }

    @Test
    void rejectsProcedureWithoutName() {
        SyntaxException exception = syntaxErrorOf("program p; procedure ; begin x := 1; end; begin x := 1; end.");

        assertEquals("faltou o nome do procedimento (linha 1, coluna 22)", exception.getMessage());
    }

    @Test
    void rejectsFunctionWithoutReturnType() {
        SyntaxException exception = syntaxErrorOf(
                "program p; function dobro(n : integer); begin dobro := 1; end; begin x := 1; end.");

        assertEquals("faltou ':' antes do tipo de retorno da função (linha 1, coluna 39)",
                exception.getMessage());
    }

    @Test
    void rejectsParameterListWithoutNameAfterSemicolon() {
        SyntaxException exception = syntaxErrorOf(
                "program p; procedure mostra(a : integer; begin x := 1; end; begin x := 1; end.");

        assertEquals("faltou o nome do parâmetro (linha 1, coluna 42)", exception.getMessage());
    }

    @Test
    void acceptsIfWithoutElse() {
        assertDoesNotThrow(() -> parse(
                "program p; begin if (x > 0) then begin y := 1; end; end."));
    }

    @Test
    void acceptsIfWithElse() {
        assertDoesNotThrow(() -> parse(
                "program p; begin if (x = y) then begin y := 1; end else begin y := 2; end; end."));
    }

    @Test
    void acceptsAllSixRelationalOperators() {
        assertDoesNotThrow(() -> parse("""
                program p;
                begin
                    if (a = b) then begin x := 1; end;
                    if (a > b) then begin x := 1; end;
                    if (a >= b) then begin x := 1; end;
                    if (a < b) then begin x := 1; end;
                    if (a <= b) then begin x := 1; end;
                    if (a <> b) then begin x := 1; end;
                end.
                """));
    }

    @Test
    void acceptsLogicalOperators() {
        assertDoesNotThrow(() -> parse(
                "program p; begin if ((a > b) and (c < d) or not (e = f)) then begin x := 1; end; end."));
    }

    @Test
    void acceptsBooleanLiterals() {
        assertDoesNotThrow(() -> parse(
                "program p; begin if (true) then begin x := 1; end; if (false) then begin x := 2; end; end."));
    }

    @Test
    void acceptsWhile() {
        assertDoesNotThrow(() -> parse(
                "program p; begin while (x < 10) do begin x := x + 1; end; end."));
    }

    @Test
    void acceptsRepeat() {
        assertDoesNotThrow(() -> parse(
                "program p; begin repeat x := x + 1; until (x >= 10); end."));
    }

    @Test
    void acceptsNestedControlStructures() {
        assertDoesNotThrow(() -> parse("""
                program p;
                begin
                    while (a < b) do
                    begin
                        if (a > 0) then
                        begin
                            repeat a := a + 1; until (a >= b);
                        end;
                    end;
                end.
                """));
    }

    @Test
    void rejectsIfWithoutParenthesis() {
        SyntaxException exception = syntaxErrorOf("program p; begin if x > 0 then begin y := 1; end; end.");

        assertEquals("faltou '(' depois de 'if' (linha 1, coluna 21)", exception.getMessage());
    }

    @Test
    void rejectsIfWithoutThen() {
        SyntaxException exception = syntaxErrorOf("program p; begin if (x > 0) begin y := 1; end; end.");

        assertEquals("faltou 'then' no 'if' (linha 1, coluna 29)", exception.getMessage());
    }

    @Test
    void rejectsWhileWithoutDo() {
        SyntaxException exception = syntaxErrorOf("program p; begin while (x < 10) begin x := 1; end; end.");

        assertEquals("faltou 'do' no 'while' (linha 1, coluna 33)", exception.getMessage());
    }

    @Test
    void rejectsRepeatWithoutUntil() {
        SyntaxException exception = syntaxErrorOf("program p; begin repeat x := 1; end.");

        assertEquals("faltou 'until' no 'repeat' (linha 1, coluna 33)", exception.getMessage());
    }

    @Test
    void rejectsConditionWithoutRelationalOperator() {
        SyntaxException exception = syntaxErrorOf("program p; begin if (x) then begin y := 1; end; end.");

        assertEquals("esperado um operador relacional (linha 1, coluna 23)", exception.getMessage());
    }

    @Test
    void rejectsParenthesizedArithmeticOnTheLeftOfComparison() {
        SyntaxException exception = syntaxErrorOf(
                "program p; begin if ((a + b) > c) then begin x := 1; end; end.");

        assertEquals("esperado um operador relacional (linha 1, coluna 28)", exception.getMessage());
    }

    @Test
    void acceptsRead() {
        assertDoesNotThrow(() -> parse("program p; begin read(x); read(a, b, c); end."));
    }

    @Test
    void acceptsWriteWithEachArgumentKind() {
        assertDoesNotThrow(() -> parse(
                "program p; begin write(x); write('texto'); write(42); write(x, 'texto', 42); end."));
    }

    @Test
    void acceptsWriteln() {
        assertDoesNotThrow(() -> parse("program p; begin writeln('resultado: ', x); end."));
    }

    @Test
    void acceptsFor() {
        assertDoesNotThrow(() -> parse(
                "program p; begin for i := 1 to 10 do begin x := x + i; end; end."));
    }

    @Test
    void acceptsForWithExpressionBounds() {
        assertDoesNotThrow(() -> parse(
                "program p; begin for i := a + 1 to b * 2 do begin x := i; end; end."));
    }

    @Test
    void rejectsReadWithoutParenthesis() {
        SyntaxException exception = syntaxErrorOf("program p; begin read x; end.");

        assertEquals("faltou '(' depois de 'read' (linha 1, coluna 23)", exception.getMessage());
    }

    @Test
    void rejectsReadWithLiteral() {
        SyntaxException exception = syntaxErrorOf("program p; begin read(42); end.");

        assertEquals("faltou a variável no comando 'read' (linha 1, coluna 23)",
                exception.getMessage());
    }

    @Test
    void rejectsWriteWithExpression() {
        SyntaxException exception = syntaxErrorOf("program p; begin write(x + 1); end.");

        assertEquals("faltou ')' no comando de escrita (linha 1, coluna 26)",
                exception.getMessage());
    }

    @Test
    void rejectsWriteWithoutArgument() {
        SyntaxException exception = syntaxErrorOf("program p; begin write(); end.");

        assertEquals("esperado identificador, cadeia ou número no comando de escrita (linha 1, coluna 24)",
                exception.getMessage());
    }

    @Test
    void rejectsForWithoutTo() {
        SyntaxException exception = syntaxErrorOf(
                "program p; begin for i := 1 do begin x := 1; end; end.");

        assertEquals("faltou 'to' no 'for' (linha 1, coluna 29)", exception.getMessage());
    }

    @Test
    void rejectsForWithoutAssignment() {
        SyntaxException exception = syntaxErrorOf(
                "program p; begin for i = 1 to 10 do begin x := 1; end; end.");

        assertEquals("faltou ':=' no 'for' (linha 1, coluna 24)", exception.getMessage());
    }

    @Test
    void acceptsFibonacciSample() throws IOException {
        Path sample = Path.of("samples/teste.pas");
        try (Reader source = Files.newBufferedReader(sample, StandardCharsets.UTF_8)) {
            Parser parser = new Parser(new Lexer(source));
            assertDoesNotThrow(parser::parse);
        }
    }
}
