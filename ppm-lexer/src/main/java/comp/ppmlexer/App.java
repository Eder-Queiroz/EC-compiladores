package comp.ppmlexer;

import comp.ppmlexer.lexer.LexicalException;
import comp.ppmlexer.lexer.Lexer;
import comp.ppmlexer.lexer.Token;
import comp.ppmlexer.lexer.TokenType;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class App {

    private static final String DEFAULT_SAMPLE = "samples/sample.ppm";

    public static void main(String[] args) {
        Path sourceFile = Path.of(args.length > 0 ? args[0] : DEFAULT_SAMPLE);
        if (args.length == 0) {
            System.out.println("Modo de usar: java -jar ppm-lexer.jar <arquivo>");
            System.out.println("Analisando o exemplo padrão: " + DEFAULT_SAMPLE);
            System.out.println();
        }
        try (Reader source = Files.newBufferedReader(sourceFile, StandardCharsets.UTF_8)) {
            printTokens(new Lexer(source));
        } catch (LexicalException exception) {
            System.err.println("Erro léxico: " + exception.getMessage());
            System.exit(1);
        } catch (IOException exception) {
            System.err.println("Não foi possível ler o arquivo: " + sourceFile);
            System.exit(1);
        }
    }

    private static void printTokens(Lexer lexer) throws LexicalException {
        Token token;
        do {
            token = lexer.nextToken();
            System.out.println(token);
        } while (token.type() != TokenType.EOF);
    }
}
