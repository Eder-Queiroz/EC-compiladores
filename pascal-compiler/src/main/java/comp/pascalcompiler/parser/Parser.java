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
        if (accept(TokenType.VAR)) {
            variableDeclaration();
            moreDeclarations();
        }
    }

    private void moreDeclarations() throws CompilationException {
        expect(TokenType.SEMICOLON, "faltou ';' depois da declaração de variáveis");
        declarationsTail();
    }

    private void declarationsTail() throws CompilationException {
        if (check(TokenType.ID)) {
            variableDeclaration();
            moreDeclarations();
        }
    }

    private void variableDeclaration() throws CompilationException {
        variables();
        expect(TokenType.COLON, "faltou ':' antes do tipo da variável");
        variableType();
    }

    private void variableType() throws CompilationException {
        expect(TokenType.INTEGER, "o único tipo aceito é 'integer'");
    }

    private void variables() throws CompilationException {
        expect(TokenType.ID, "faltou o nome da variável");
        moreVariables();
    }

    private void moreVariables() throws CompilationException {
        if (accept(TokenType.COMMA)) {
            variables();
        }
    }

    private void routine() throws CompilationException {
        if (check(TokenType.PROCEDURE)) {
            procedureDeclaration();
            return;
        }
        if (check(TokenType.FUNCTION)) {
            functionDeclaration();
        }
    }

    private void procedureDeclaration() throws CompilationException {
        expect(TokenType.PROCEDURE, "esperado 'procedure'");
        expect(TokenType.ID, "faltou o nome do procedimento");
        parameters();
        expect(TokenType.SEMICOLON, "faltou ';' depois do cabeçalho do procedimento");
        body();
        expect(TokenType.SEMICOLON, "faltou ';' depois do corpo do procedimento");
        routine();
    }

    private void functionDeclaration() throws CompilationException {
        expect(TokenType.FUNCTION, "esperado 'function'");
        expect(TokenType.ID, "faltou o nome da função");
        parameters();
        expect(TokenType.COLON, "faltou ':' antes do tipo de retorno da função");
        functionType();
        expect(TokenType.SEMICOLON, "faltou ';' depois do cabeçalho da função");
        body();
        expect(TokenType.SEMICOLON, "faltou ';' depois do corpo da função");
        routine();
    }

    private void functionType() throws CompilationException {
        expect(TokenType.INTEGER, "o único tipo de retorno aceito é 'integer'");
    }

    private void parameters() throws CompilationException {
        if (accept(TokenType.LEFT_PAREN)) {
            parameterList();
            expect(TokenType.RIGHT_PAREN, "faltou ')' na lista de parâmetros");
        }
    }

    private void parameterList() throws CompilationException {
        identifierList();
        expect(TokenType.COLON, "faltou ':' antes do tipo do parâmetro");
        variableType();
        parameterListTail();
    }

    private void parameterListTail() throws CompilationException {
        if (accept(TokenType.SEMICOLON)) {
            parameterList();
        }
    }

    private void identifierList() throws CompilationException {
        expect(TokenType.ID, "faltou o nome do parâmetro");
        identifierListTail();
    }

    private void identifierListTail() throws CompilationException {
        if (accept(TokenType.COMMA)) {
            identifierList();
        }
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
