package comp.mtllexer.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

public final class SourceReader {

    private static final int END_OF_STREAM = -1;

    private final BufferedReader reader;
    private int currentCharacter;
    private int line;
    private int column;

    public SourceReader(Reader source) {
        this.reader = new BufferedReader(source);
        this.line = 1;
        this.column = 1;
        this.currentCharacter = read();
    }

    public char current() {
        return (char) currentCharacter;
    }

    public boolean isAtEnd() {
        return currentCharacter == END_OF_STREAM;
    }

    public void advance() {
        if (isAtEnd()) {
            return;
        }
        if (currentCharacter == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        currentCharacter = read();
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    private int read() {
        try {
            return reader.read();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
