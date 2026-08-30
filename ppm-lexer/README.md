# Analisador Léxico — PPM (Portable Pixmap)

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
mvn package && java -jar target/ppm-lexer.jar samples/sample.ppm
```

Sem argumento, o programa imprime o modo de usar e analisa `samples/sample.ppm`.

## Saída

Um token por linha, no formato `Token [linha, coluna, classe, valor]`. A posição
é a do primeiro caractere do lexema. Tokens sem atributo (`EOF`) omitem o valor.

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
| `App` | linha de comando: imprime os tokens e traduz erro em código de saída |

O analisador é escrito à mão, sem expressões regulares e sem gerador léxico.
