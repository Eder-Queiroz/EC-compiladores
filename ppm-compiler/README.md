# Compilador PPM (Portable Pixmap)

Reconhece os itens léxicos de imagens PPM em modo ASCII (`P3`), onde a imagem é
literalmente um arquivo de texto estruturado.

```
P3              <- número mágico que identifica o formato
3 2             <- largura e altura em pixels
255             <- valor máximo de cor
255 0 0   0 255 0   0 0 255      <- linha 1: pixel vermelho, verde e azul
255 255 0 255 255 255 0 0 0      <- linha 2: pixel amarelo, branco e preto
```

## Tokens

| Classe | Descrição |
|---|---|
| `MAGIC` | a cadeia exata `P3` |
| `NUMERO` | inteiro positivo (`0`, `128`, `255`) |
| `EOF` | fim do arquivo |

Regras adicionais:

- `#` inicia um comentário, ignorado até o fim da linha. Vale também no fim de
  uma linha que já tem conteúdo (`255 # cor máxima`).
- Espaço, tabulação, `\r` e `\n` são separadores e não geram token.
- Qualquer outro caractere é erro léxico.
- Um número mágico diferente de `P3` (por exemplo `P6`, que é o PPM binário) é
  erro léxico.

## Como executar

Pré-requisitos: Java 17 e Maven.

```bash
mvn test
```

```bash
mvn package && java -jar target/ppm-compiler.jar samples/sample.ppm
```

Sem argumento, o programa imprime o modo de usar e analisa `samples/sample.ppm`.

## Saída

O modo padrão (análise sintática) imprime uma linha de sucesso ou o erro
encontrado:

```
Análise sintática concluída sem erros: samples/sample.ppm
```

Com `--tokens`, o programa lista um token por linha, no formato
`Token [linha, coluna, classe, valor]`. A posição é a do primeiro caractere do
lexema. Tokens sem atributo (`EOF`) omitem o valor.

```
Token [1, 1, classe=MAGIC, valor=P3]
Token [3, 1, classe=NUMERO, valor=3]
Token [3, 3, classe=NUMERO, valor=2]
Token [4, 1, classe=NUMERO, valor=255]
Token [5, 1, classe=NUMERO, valor=255]
Token [5, 5, classe=NUMERO, valor=0]
Token [5, 7, classe=NUMERO, valor=0]
Token [5, 12, classe=NUMERO, valor=0]
Token [5, 14, classe=NUMERO, valor=255]
Token [5, 18, classe=NUMERO, valor=0]
Token [5, 23, classe=NUMERO, valor=0]
Token [5, 25, classe=NUMERO, valor=0]
Token [5, 27, classe=NUMERO, valor=255]
Token [6, 1, classe=NUMERO, valor=255]
Token [6, 5, classe=NUMERO, valor=255]
Token [6, 9, classe=NUMERO, valor=0]
Token [6, 12, classe=NUMERO, valor=255]
Token [6, 16, classe=NUMERO, valor=255]
Token [6, 20, classe=NUMERO, valor=255]
Token [6, 25, classe=NUMERO, valor=0]
Token [6, 27, classe=NUMERO, valor=0]
Token [6, 29, classe=NUMERO, valor=0]
Token [7, 1, classe=EOF]
```

A linha 2 do arquivo é um comentário e por isso não aparece na saída.

## Erros

Ao encontrar um caractere inválido o analisador imprime a mensagem em `stderr`,
com linha e coluna, e encerra com código de saída 1.

```
Erro léxico: caractere inválido '@' (linha 2, coluna 1)
```

## Organização do código

| Arquivo | Responsabilidade |
|---|---|
| `lexer/SourceReader` | lê o arquivo caractere a caractere e mantém linha e coluna |
| `lexer/TokenType` | as classes de token do formato |
| `lexer/TokenValue` | o atributo do token: texto ou número inteiro |
| `lexer/Token` | classe, valor e posição |
| `lexer/Lexer` | o autômato: descarta separadores e comentários e reconhece os lexemas |
| `lexer/LexicalException` | erro léxico com posição |
| `CompilationException` | superclasse abstrata dos erros de compilação, guarda linha e coluna |
| `parser/Parser` | analisador sintático recursivo descendente |
| `parser/SyntaxException` | erro sintático com posição |
| `App` | linha de comando: analisa por padrão, lista os tokens com `--tokens` |

O analisador é escrito à mão, sem expressões regulares e sem gerador léxico.

## Análise sintática

Analisa a estrutura de imagens PPM usando um analisador sintático descendente recursivo.

### Gramática

```
<image>         ::= MAGIC <dimensions> <maximum_color> <pixels>
<dimensions>    ::= NUMERO NUMERO
<maximum_color> ::= NUMERO
<pixels>        ::= <pixel> <pixels> | ε
<pixel>         ::= NUMERO NUMERO NUMERO
```

### Mapeamento de não-terminais para métodos

| Não-terminal | Método |
|---|---|
| `<image>` | `image()` |
| `<dimensions>` | `dimensions()` |
| `<maximum_color>` | `maximumColor()` |
| `<pixels>` | `pixels()` |
| `<pixel>` | `pixel()` |

**Nota:** A validação de contagem de pixels contra largura × altura é análise semântica e fica fora do escopo do analisador sintático.

### Iteração em vez de recursão

A gramática acima escreve `<pixels> ::= <pixel> <pixels> | ε` como recursão à
direita, mas `pixels()` é implementado com um laço `while`, não com uma
chamada recursiva a si mesmo. Uma imagem PPM pode ter milhões de pixels, e
recursão própria consumiria um quadro de pilha por repetição — um
`StackOverflowError` certo em qualquer imagem grande. O laço aceita
exatamente a mesma linguagem, já que iteração é a transformação padrão de uma
recursão de cauda. O analisador de Pascal faz o oposto de propósito: lá a
repetição segue a estrutura do programa (poucas declarações, poucos
comandos), então a recursão própria é natural e não corre risco de estourar
a pilha.

### Execução

O programa oferece dois modos:

1. **Análise sintática completa** (padrão):
   ```bash
   java -jar target/ppm-compiler.jar <arquivo>
   ```
   Analisa o arquivo e imprime sucesso ou erro com posição.

2. **Modo tokens** (debug):
   ```bash
   java -jar target/ppm-compiler.jar <arquivo> --tokens
   ```
   Imprime todos os tokens do arquivo.

### Exemplos

Análise sintática bem-sucedida:
```
$ java -jar target/ppm-compiler.jar samples/sample.ppm
Análise sintática concluída sem erros: samples/sample.ppm
```

Modo tokens (última linha):
```
$ java -jar target/ppm-compiler.jar samples/sample.ppm --tokens | tail -1
Token [7, 1, classe=EOF]
```

Erro sintático:
```
$ printf 'P3 1 1 255 0 0' > /tmp/curto.ppm
$ java -jar target/ppm-compiler.jar /tmp/curto.ppm
Erro sintático: faltou a componente azul do pixel (linha 1, coluna 15)
```
