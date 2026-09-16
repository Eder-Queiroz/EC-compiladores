package comp.pascalcompiler.parser;

import comp.pascalcompiler.CompilationException;
import comp.pascalcompiler.lexer.Lexer;
import comp.pascalcompiler.lexer.Token;
import comp.pascalcompiler.lexer.TokenType;

public final class Parser {

    private final Lexer lexer;
    private Token token;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public void parse() throws CompilationException {
        advance();
        program();
        expect(TokenType.EOF, "esperado o fim do arquivo");
    }

    private void program() throws CompilationException {
        expect(TokenType.PROGRAM, "o programa deve começar com 'program'");
        expect(TokenType.ID, "faltou o nome do programa");
        expect(TokenType.SEMICOLON, "faltou ';' depois do nome do programa");
        body();
        expect(TokenType.DOT, "faltou o '.' no final do programa");
    }

    private void body() throws CompilationException {
        declarations();
        routine();
        expect(TokenType.BEGIN, "faltou 'begin'");
        statements();
        expect(TokenType.END, "faltou 'end'");
    }

    private void declarations() throws CompilationException {
    }

    private void routine() throws CompilationException {
    }

    private void statements() throws CompilationException {
        statement();
        moreStatements();
    }

    private void moreStatements() throws CompilationException {
        expect(TokenType.SEMICOLON, "faltou ';' depois do comando");
        statementsTail();
    }

    private void statementsTail() throws CompilationException {
        if (check(TokenType.ID)) {
            statements();
        }
    }

    private void statement() throws CompilationException {
        expect(TokenType.ID, "esperado um comando");
        statementTail();
    }

    private void statementTail() throws CompilationException {
        if (accept(TokenType.ASSIGN)) {
            expression();
            return;
        }
        arguments();
    }

    private void arguments() throws CompilationException {
        if (accept(TokenType.LEFT_PAREN)) {
            argumentList();
            expect(TokenType.RIGHT_PAREN, "faltou ')' na lista de argumentos");
        }
    }

    private void argumentList() throws CompilationException {
        expression();
        argumentListTail();
    }

    private void argumentListTail() throws CompilationException {
        if (accept(TokenType.COMMA)) {
            argumentList();
        }
    }

    private void expression() throws CompilationException {
        term();
        moreExpression();
    }

    private void moreExpression() throws CompilationException {
        if (accept(TokenType.PLUS) || accept(TokenType.MINUS)) {
            term();
            moreExpression();
        }
    }

    private void term() throws CompilationException {
        factor();
        moreTerm();
    }

    private void moreTerm() throws CompilationException {
        if (accept(TokenType.TIMES) || accept(TokenType.DIVIDE)) {
            factor();
            moreTerm();
        }
    }

    private void factor() throws CompilationException {
        if (accept(TokenType.ID)) {
            arguments();
            return;
        }
        if (accept(TokenType.INTNUM)) {
            return;
        }
        expect(TokenType.LEFT_PAREN, "esperado identificador, número ou '(' na expressão");
        expression();
        expect(TokenType.RIGHT_PAREN, "faltou ')' na expressão");
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
