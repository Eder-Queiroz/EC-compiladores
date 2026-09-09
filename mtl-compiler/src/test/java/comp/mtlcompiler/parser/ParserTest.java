package comp.mtlcompiler.parser;

import comp.mtlcompiler.CompilationException;
import comp.mtlcompiler.lexer.Lexer;
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
    void acceptsEmptyLibrary() {
        assertDoesNotThrow(() -> parse(""));
    }

    @Test
    void acceptsMaterialWithoutProperties() {
        assertDoesNotThrow(() -> parse("newmtl texture"));
    }

    @Test
    void acceptsAllProperties() {
        assertDoesNotThrow(() -> parse("""
                newmtl texture
                Ka 0.0 0.0 0.0
                Kd 0.5 0.5 0.5
                Ks 0.0 0.0 0.0
                Ns 10.0
                illum 2
                map_Kd texture.ppm
                """));
    }

    @Test
    void acceptsSeveralMaterials() {
        assertDoesNotThrow(() -> parse("newmtl um\nKd 1 1 1\nnewmtl dois\nNs 5.0"));
    }

    @Test
    void acceptsIntegerWhereFloatIsExpected() {
        assertDoesNotThrow(() -> parse("newmtl m\nKd 1 0 0"));
    }

    @Test
    void acceptsProjectSample() throws IOException {
        Path sample = Path.of("samples/cube.mtl");
        try (Reader source = Files.newBufferedReader(sample, StandardCharsets.UTF_8)) {
            Parser parser = new Parser(new Lexer(source));
            assertDoesNotThrow(parser::parse);
        }
    }

    @Test
    void rejectsMaterialWithoutName() {
        SyntaxException exception = syntaxErrorOf("newmtl\nKd 1 1 1");

        assertEquals("faltou o nome do material depois de 'newmtl' (linha 2, coluna 1)",
                exception.getMessage());
    }

    @Test
    void rejectsPropertyBeforeFirstMaterial() {
        SyntaxException exception = syntaxErrorOf("Kd 1 1 1");

        assertEquals("esperado o fim do arquivo (linha 1, coluna 1)", exception.getMessage());
    }

    @Test
    void rejectsIncompleteColor() {
        SyntaxException exception = syntaxErrorOf("newmtl m\nKa 0.0 0.0");

        assertEquals("faltou a componente azul da cor (linha 2, coluna 11)", exception.getMessage());
    }

    @Test
    void rejectsFloatForIllum() {
        SyntaxException exception = syntaxErrorOf("newmtl m\nillum 2.0");

        assertEquals("'illum' exige um número inteiro (linha 2, coluna 7)", exception.getMessage());
    }

    @Test
    void rejectsMapWithoutFileName() {
        SyntaxException exception = syntaxErrorOf("newmtl m\nmap_Kd 3");

        assertEquals("faltou o nome do arquivo de textura depois de 'map_Kd' (linha 2, coluna 8)",
                exception.getMessage());
    }
}
