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
        if (check(TokenType.ID, TokenType.IF, TokenType.WHILE, TokenType.REPEAT,
                TokenType.READ, TokenType.WRITE, TokenType.WRITELN, TokenType.FOR)) {
            statements();
        }
    }

    private void statement() throws CompilationException {
        if (accept(TokenType.IF)) {
            expect(TokenType.LEFT_PAREN, "faltou '(' depois de 'if'");
            logicalExpression();
            expect(TokenType.RIGHT_PAREN, "faltou ')' na condição do 'if'");
            expect(TokenType.THEN, "faltou 'then' no 'if'");
            expect(TokenType.BEGIN, "faltou 'begin' depois de 'then'");
            statements();
            expect(TokenType.END, "faltou 'end' no bloco do 'then'");
            elsePart();
            return;
        }
        if (accept(TokenType.WHILE)) {
            expect(TokenType.LEFT_PAREN, "faltou '(' depois de 'while'");
            logicalExpression();
            expect(TokenType.RIGHT_PAREN, "faltou ')' na condição do 'while'");
            expect(TokenType.DO, "faltou 'do' no 'while'");
            expect(TokenType.BEGIN, "faltou 'begin' no corpo do 'while'");
            statements();
            expect(TokenType.END, "faltou 'end' no corpo do 'while'");
            return;
        }
        if (accept(TokenType.REPEAT)) {
            statements();
            expect(TokenType.UNTIL, "faltou 'until' no 'repeat'");
            expect(TokenType.LEFT_PAREN, "faltou '(' depois de 'until'");
            logicalExpression();
            expect(TokenType.RIGHT_PAREN, "faltou ')' na condição do 'until'");
            return;
        }
        if (accept(TokenType.READ)) {
            expect(TokenType.LEFT_PAREN, "faltou '(' depois de 'read'");
            readArguments();
            expect(TokenType.RIGHT_PAREN, "faltou ')' no comando 'read'");
            return;
        }
        if (accept(TokenType.WRITE) || accept(TokenType.WRITELN)) {
            expect(TokenType.LEFT_PAREN, "faltou '(' no comando de escrita");
            writeArguments();
            expect(TokenType.RIGHT_PAREN, "faltou ')' no comando de escrita");
            return;
        }
        if (accept(TokenType.FOR)) {
            expect(TokenType.ID, "faltou a variável de controle do 'for'");
            expect(TokenType.ASSIGN, "faltou ':=' no 'for'");
            expression();
            expect(TokenType.TO, "faltou 'to' no 'for'");
            expression();
            expect(TokenType.DO, "faltou 'do' no 'for'");
            expect(TokenType.BEGIN, "faltou 'begin' no corpo do 'for'");
            statements();
            expect(TokenType.END, "faltou 'end' no corpo do 'for'");
            return;
        }
        expect(TokenType.ID, "esperado um comando");
        statementTail();
    }

    private void elsePart() throws CompilationException {
        if (accept(TokenType.ELSE)) {
            expect(TokenType.BEGIN, "faltou 'begin' depois de 'else'");
            statements();
            expect(TokenType.END, "faltou 'end' no bloco do 'else'");
        }
    }

    private void logicalExpression() throws CompilationException {
        logicalTerm();
        moreLogicalExpression();
    }

    private void moreLogicalExpression() throws CompilationException {
        if (accept(TokenType.OR)) {
            logicalTerm();
            moreLogicalExpression();
        }
    }

    private void logicalTerm() throws CompilationException {
        logicalFactor();
        moreLogicalTerm();
    }

    private void moreLogicalTerm() throws CompilationException {
        if (accept(TokenType.AND)) {
            logicalFactor();
            moreLogicalTerm();
        }
    }

    private void logicalFactor() throws CompilationException {
        if (accept(TokenType.LEFT_PAREN)) {
            logicalExpression();
            expect(TokenType.RIGHT_PAREN, "faltou ')' na expressão lógica");
            return;
        }
        if (accept(TokenType.NOT)) {
            logicalFactor();
            return;
        }
        if (accept(TokenType.TRUE) || accept(TokenType.FALSE)) {
            return;
        }
        relational();
    }

    private void relational() throws CompilationException {
        expression();
        if (accept(TokenType.EQUAL) || accept(TokenType.GREATER) || accept(TokenType.GREATER_EQUAL)
                || accept(TokenType.LESS) || accept(TokenType.LESS_EQUAL)
                || accept(TokenType.NOT_EQUAL)) {
            expression();
            return;
        }
        throw new SyntaxException("esperado um operador relacional", token.line(), token.column());
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

    private void readArguments() throws CompilationException {
        expect(TokenType.ID, "faltou a variável no comando 'read'");
        moreReadArguments();
    }

    private void moreReadArguments() throws CompilationException {
        if (accept(TokenType.COMMA)) {
            readArguments();
        }
    }

    private void writeArguments() throws CompilationException {
        if (accept(TokenType.ID) || accept(TokenType.STRING) || accept(TokenType.INTNUM)) {
            moreWriteArguments();
            return;
        }
        throw new SyntaxException("esperado identificador, cadeia ou número no comando de escrita",
                token.line(), token.column());
    }

    private void moreWriteArguments() throws CompilationException {
        if (accept(TokenType.COMMA)) {
            writeArguments();
        }
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
