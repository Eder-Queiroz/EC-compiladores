package comp.objcompiler.parser;

import comp.objcompiler.CompilationException;
import comp.objcompiler.lexer.Lexer;
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
    void acceptsEmptyModel() {
        assertDoesNotThrow(() -> parse(""));
    }

    @Test
    void acceptsDeclarationCommands() {
        assertDoesNotThrow(() -> parse("mtllib cube.mtl\ng cube\no caixa\nusemtl texture"));
    }

    @Test
    void acceptsVertexCommands() {
        assertDoesNotThrow(() -> parse("v 0.0 1.0 2.0\nvn -1.0 0.0 0.0\nvt 0.25 1.00"));
    }

    @Test
    void rejectsIntegerWhereFloatIsExpected() {
        SyntaxException exception = syntaxErrorOf("vt 0 0.75");

        assertEquals("faltou a coordenada u da textura (linha 1, coluna 4)",
                exception.getMessage());
    }

    @Test
    void acceptsFaceWithVertexOnly() {
        assertDoesNotThrow(() -> parse("f 3 7 8"));
    }

    @Test
    void acceptsFaceWithVertexAndTexture() {
        assertDoesNotThrow(() -> parse("f 3/10 7/6 8/5"));
    }

    @Test
    void acceptsFaceWithVertexAndNormal() {
        assertDoesNotThrow(() -> parse("f 3//1 7//1 8//1"));
    }

    @Test
    void acceptsFaceWithAllThreeReferences() {
        assertDoesNotThrow(() -> parse("f 3/10/1 7/6/1 8/5/1"));
    }

    @Test
    void rejectsFaceWithMoreThanThreeVertices() {
        SyntaxException exception = syntaxErrorOf("f 1/1/1 2/2/2 3/3/3 4/4/4");

        assertEquals("esperado o fim do arquivo (linha 1, coluna 21)", exception.getMessage());
    }

    @Test
    void acceptsProjectSample() throws IOException {
        Path sample = Path.of("samples/cube.obj");
        try (Reader source = Files.newBufferedReader(sample, StandardCharsets.UTF_8)) {
            Parser parser = new Parser(new Lexer(source));
            assertDoesNotThrow(parser::parse);
        }
    }

    @Test
    void rejectsFaceWithTwoVertices() {
        SyntaxException exception = syntaxErrorOf("f 1/1/1 2/2/2");

        assertEquals("faltou o índice do vértice na face (linha 1, coluna 14)", exception.getMessage());
    }

    @Test
    void rejectsMaterialLibraryWithoutName() {
        SyntaxException exception = syntaxErrorOf("mtllib 3");

        assertEquals("faltou o nome do arquivo de material depois de 'mtllib' (linha 1, coluna 8)",
                exception.getMessage());
    }

    @Test
    void rejectsIncompleteVertex() {
        SyntaxException exception = syntaxErrorOf("v 0.0 1.0");

        assertEquals("faltou a coordenada z do vértice (linha 1, coluna 10)", exception.getMessage());
    }

    @Test
    void rejectsTextureVertexWithThreeNumbers() {
        SyntaxException exception = syntaxErrorOf("vt 0.25 0.50 0.75");

        assertEquals("esperado o fim do arquivo (linha 1, coluna 14)", exception.getMessage());
    }

    @Test
    void rejectsSlashWithoutIndex() {
        SyntaxException exception = syntaxErrorOf("f 1/ g cube");

        assertEquals("faltou o índice da textura depois de '/' (linha 1, coluna 6)",
                exception.getMessage());
    }
}
