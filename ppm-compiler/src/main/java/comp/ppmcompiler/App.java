package comp.ppmcompiler;

import comp.ppmcompiler.lexer.LexicalException;
import comp.ppmcompiler.lexer.Lexer;
import comp.ppmcompiler.lexer.Token;
import comp.ppmcompiler.lexer.TokenType;
import comp.ppmcompiler.parser.Parser;
import comp.ppmcompiler.parser.SyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class App {

    private static final String DEFAULT_SAMPLE = "samples/sample.ppm";
    private static final String TOKENS_OPTION = "--tokens";

    public static void main(String[] args) {
        Path sourceFile = Path.of(args.length > 0 && !TOKENS_OPTION.equals(args[0]) ? args[0] : DEFAULT_SAMPLE);
        boolean tokensOnly = args.length > 0 && TOKENS_OPTION.equals(args[args.length - 1]);
        if (args.length == 0) {
            System.out.println("Modo de usar: java -jar ppm-compiler.jar <arquivo> [--tokens]");
            System.out.println("Analisando o exemplo padrão: " + DEFAULT_SAMPLE);
            System.out.println();
        }
        try (Reader source = Files.newBufferedReader(sourceFile, StandardCharsets.UTF_8)) {
            if (tokensOnly) {
                printTokens(new Lexer(source));
            } else {
                new Parser(new Lexer(source)).parse();
                System.out.println("Análise sintática concluída sem erros: " + sourceFile);
            }
        } catch (SyntaxException exception) {
            System.err.println("Erro sintático: " + exception.getMessage());
            System.exit(1);
        } catch (LexicalException exception) {
            System.err.println("Erro léxico: " + exception.getMessage());
            System.exit(1);
        } catch (CompilationException exception) {
            System.err.println("Erro de compilação: " + exception.getMessage());
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
