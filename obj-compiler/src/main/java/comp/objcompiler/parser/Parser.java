package comp.objcompiler.parser;

import comp.objcompiler.CompilationException;
import comp.objcompiler.lexer.Lexer;
import comp.objcompiler.lexer.Token;
import comp.objcompiler.lexer.TokenType;

public final class Parser {

    private final Lexer lexer;
    private Token token;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public void parse() throws CompilationException {
        advance();
        model();
        expect(TokenType.EOF, "esperado o fim do arquivo");
    }

    private void model() throws CompilationException {
        commands();
    }

    private void commands() throws CompilationException {
        while (check(TokenType.KW_MTLLIB, TokenType.KW_USEMTL, TokenType.KW_G, TokenType.KW_O,
                TokenType.KW_V, TokenType.KW_VN, TokenType.KW_VT, TokenType.KW_F)) {
            command();
        }
    }

    private void command() throws CompilationException {
        if (accept(TokenType.KW_MTLLIB)) {
            expect(TokenType.IDENTIFICADOR, "faltou o nome do arquivo de material depois de 'mtllib'");
            return;
        }
        if (accept(TokenType.KW_USEMTL)) {
            expect(TokenType.IDENTIFICADOR, "faltou o nome do material depois de 'usemtl'");
            return;
        }
        if (accept(TokenType.KW_G)) {
            expect(TokenType.IDENTIFICADOR, "faltou o nome do grupo depois de 'g'");
            return;
        }
        if (accept(TokenType.KW_O)) {
            expect(TokenType.IDENTIFICADOR, "faltou o nome do objeto depois de 'o'");
            return;
        }
        if (accept(TokenType.KW_V)) {
            number("faltou a coordenada x do vértice");
            number("faltou a coordenada y do vértice");
            number("faltou a coordenada z do vértice");
            return;
        }
        if (accept(TokenType.KW_VN)) {
            number("faltou a componente x do vetor normal");
            number("faltou a componente y do vetor normal");
            number("faltou a componente z do vetor normal");
            return;
        }
        if (accept(TokenType.KW_VT)) {
            number("faltou a coordenada u da textura");
            number("faltou a coordenada v da textura");
            return;
        }
        expect(TokenType.KW_F, "esperado um comando do formato OBJ");
        vertex();
        vertex();
        vertex();
        moreVertices();
    }

    private void moreVertices() throws CompilationException {
        while (check(TokenType.INTEIRO)) {
            vertex();
        }
    }

    private void vertex() throws CompilationException {
        expect(TokenType.INTEIRO, "faltou o índice do vértice na face");
        references();
    }

    private void references() throws CompilationException {
        if (accept(TokenType.BARRA)) {
            textureReference();
        }
    }

    private void textureReference() throws CompilationException {
        if (accept(TokenType.BARRA)) {
            expect(TokenType.INTEIRO, "faltou o índice da normal depois de '//'");
            return;
        }
        expect(TokenType.INTEIRO, "faltou o índice da textura depois de '/'");
        normalReference();
    }

    private void normalReference() throws CompilationException {
        if (accept(TokenType.BARRA)) {
            expect(TokenType.INTEIRO, "faltou o índice da normal depois de '/'");
        }
    }

    private void number(String message) throws CompilationException {
        if (accept(TokenType.INTEIRO) || accept(TokenType.FLOAT)) {
            return;
        }
        throw new SyntaxException(message, token.line(), token.column());
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

    private boolean accept(TokenType type) throws CompilationException {
        if (token.type() != type) {
            return false;
        }
        advance();
        return true;
    }

    private void expect(TokenType type, String message) throws CompilationException {
        if (token.type() != type) {
            throw new SyntaxException(message, token.line(), token.column());
        }
        advance();
    }
}
