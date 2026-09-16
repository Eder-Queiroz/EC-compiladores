package comp.ppmcompiler.parser;

import comp.ppmcompiler.CompilationException;
import comp.ppmcompiler.lexer.Lexer;
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
    void acceptsMinimalImage() {
        assertDoesNotThrow(() -> parse("P3 1 1 255 0 0 0"));
    }

    @Test
    void rejectsImageWithoutPixels() {
        SyntaxException exception = syntaxErrorOf("P3 0 0 255");

        assertEquals("faltou a componente vermelha do pixel (linha 1, coluna 11)",
                exception.getMessage());
    }

    @Test
    void acceptsProjectSample() throws IOException {
        Path sample = Path.of("samples/sample.ppm");
        try (Reader source = Files.newBufferedReader(sample, StandardCharsets.UTF_8)) {
            Parser parser = new Parser(new Lexer(source));
            assertDoesNotThrow(parser::parse);
        }
    }

    @Test
    void rejectsMissingMagicNumber() {
        SyntaxException exception = syntaxErrorOf("1 1 255 0 0 0");

        assertEquals("o arquivo deve começar com o número mágico 'P3' (linha 1, coluna 1)",
                exception.getMessage());
    }

    @Test
    void rejectsMissingHeight() {
        SyntaxException exception = syntaxErrorOf("P3 1");

        assertEquals("faltou a altura da imagem (linha 1, coluna 5)", exception.getMessage());
    }

    @Test
    void rejectsMissingMaximumColor() {
        SyntaxException exception = syntaxErrorOf("P3 1 1");

        assertEquals("faltou o valor máximo de cor (linha 1, coluna 7)", exception.getMessage());
    }

    @Test
    void rejectsIncompletePixel() {
        SyntaxException exception = syntaxErrorOf("P3 1 1 255 0 0");

        assertEquals("faltou a componente azul do pixel (linha 1, coluna 15)", exception.getMessage());
    }

    @Test
    void rejectsEmptyInput() {
        SyntaxException exception = syntaxErrorOf("");

        assertEquals("o arquivo deve começar com o número mágico 'P3' (linha 1, coluna 1)",
                exception.getMessage());
    }
}
