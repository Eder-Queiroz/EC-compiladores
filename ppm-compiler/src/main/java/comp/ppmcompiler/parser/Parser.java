package comp.ppmcompiler.parser;

import comp.ppmcompiler.CompilationException;
import comp.ppmcompiler.lexer.Lexer;
import comp.ppmcompiler.lexer.Token;
import comp.ppmcompiler.lexer.TokenType;

public final class Parser {

    private final Lexer lexer;
    private Token token;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public void parse() throws CompilationException {
        advance();
        image();
        expect(TokenType.EOF, "esperado o fim do arquivo");
    }

    private void image() throws CompilationException {
        expect(TokenType.MAGIC, "o arquivo deve começar com o número mágico 'P3'");
        dimensions();
        maximumColor();
        pixels();
    }

    private void dimensions() throws CompilationException {
        expect(TokenType.NUMERO, "faltou a largura da imagem");
        expect(TokenType.NUMERO, "faltou a altura da imagem");
    }

    private void maximumColor() throws CompilationException {
        expect(TokenType.NUMERO, "faltou o valor máximo de cor");
    }

    private void pixels() throws CompilationException {
        while (check(TokenType.NUMERO)) {
            pixel();
        }
    }

    private void pixel() throws CompilationException {
        expect(TokenType.NUMERO, "faltou a componente vermelha do pixel");
        expect(TokenType.NUMERO, "faltou a componente verde do pixel");
        expect(TokenType.NUMERO, "faltou a componente azul do pixel");
    }

    private void advance() throws CompilationException {
        token = lexer.nextToken();
    }

    private boolean check(TokenType... types) {
        for (TokenType type : types) {
            if (token.type() == type) {
                return true;
            }
        }
        return false;
    }

    private void expect(TokenType type, String message) throws CompilationException {
        if (token.type() != type) {
            throw new SyntaxException(message, token.line(), token.column());
        }
        advance();
    }
}
