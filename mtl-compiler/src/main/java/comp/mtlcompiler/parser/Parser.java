package comp.mtlcompiler.parser;

import comp.mtlcompiler.CompilationException;
import comp.mtlcompiler.lexer.Lexer;
import comp.mtlcompiler.lexer.Token;
import comp.mtlcompiler.lexer.TokenType;

public final class Parser {

    private final Lexer lexer;
    private Token token;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public void parse() throws CompilationException {
        advance();
        materialLibrary();
        expect(TokenType.EOF, "esperado o fim do arquivo");
    }

    private void materialLibrary() throws CompilationException {
        materials();
    }

    private void materials() throws CompilationException {
        while (check(TokenType.KW_NEWMTL)) {
            material();
        }
    }

    private void material() throws CompilationException {
        expect(TokenType.KW_NEWMTL, "esperado 'newmtl' para iniciar um material");
        expect(TokenType.IDENTIFICADOR, "faltou o nome do material depois de 'newmtl'");
        properties();
    }

    private void properties() throws CompilationException {
        while (check(TokenType.KW_KA, TokenType.KW_KD, TokenType.KW_KS,
                TokenType.KW_NS, TokenType.KW_ILLUM, TokenType.KW_MAP_KD)) {
            property();
        }
    }

    private void property() throws CompilationException {
        if (accept(TokenType.KW_KA) || accept(TokenType.KW_KD) || accept(TokenType.KW_KS)) {
            color();
            return;
        }
        if (accept(TokenType.KW_NS)) {
            expect(TokenType.FLOAT, "faltou o valor do expoente especular depois de 'Ns'");
            return;
        }
        if (accept(TokenType.KW_ILLUM)) {
            expect(TokenType.INTEIRO, "'illum' exige um número inteiro");
            return;
        }
        expect(TokenType.KW_MAP_KD, "esperada uma propriedade de material");
        expect(TokenType.IDENTIFICADOR, "faltou o nome do arquivo de textura depois de 'map_Kd'");
    }

    private void color() throws CompilationException {
        expect(TokenType.FLOAT, "faltou a componente vermelha da cor");
        expect(TokenType.FLOAT, "faltou a componente verde da cor");
        expect(TokenType.FLOAT, "faltou a componente azul da cor");
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
