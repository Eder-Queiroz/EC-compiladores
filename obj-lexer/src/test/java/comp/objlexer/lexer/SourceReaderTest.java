package comp.objlexer.lexer;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceReaderTest {

    private SourceReader readerOf(String content) {
        return new SourceReader(new StringReader(content));
    }

    @Test
    void startsAtFirstCharacterOfFirstLine() {
        SourceReader reader = readerOf("P3");

        assertEquals('P', reader.current());
        assertEquals(1, reader.line());
        assertEquals(1, reader.column());
    }

    @Test
    void advancesColumnWithinTheSameLine() {
        SourceReader reader = readerOf("P3");

        reader.advance();

        assertEquals('3', reader.current());
        assertEquals(1, reader.line());
        assertEquals(2, reader.column());
    }

    @Test
    void resetsColumnAndCountsLineOnLineBreak() {
        SourceReader reader = readerOf("P\n3");

        reader.advance();
        reader.advance();

        assertEquals('3', reader.current());
        assertEquals(2, reader.line());
        assertEquals(1, reader.column());
    }

    @Test
    void reportsEndOfInput() {
        SourceReader reader = readerOf("P");

        assertFalse(reader.isAtEnd());
        reader.advance();
        assertTrue(reader.isAtEnd());
    }

    @Test
    void isAtEndForEmptyInput() {
        assertTrue(readerOf("").isAtEnd());
    }

    @Test
    void advanceIsIdempotentAtEndOfInput() {
        SourceReader reader = readerOf("");

        reader.advance();

        assertTrue(reader.isAtEnd());
        assertEquals(1, reader.line());
        assertEquals(1, reader.column());
    }
}
