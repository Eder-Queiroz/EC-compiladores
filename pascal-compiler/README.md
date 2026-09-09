# Analisador Léxico — Pascal Simplificado

Reconhece os itens léxicos da linguagem Pascal Simplificada definida pela BNF da
disciplina. É a primeira fase do compilador: a sequência de tokens produzida aqui
é o que o analisador sintático vai consumir depois.

## Tokens

### Palavras reservadas (24)

Derivadas dos terminais da BNF. Cada uma tem uma classe homônima em maiúsculas.

| | | | |
|---|---|---|---|
| `program` | `var` | `integer` | `procedure` |
| `function` | `begin` | `end` | `read` |
| `write` | `writeln` | `for` | `to` |
| `do` | `repeat` | `until` | `while` |
| `if` | `then` | `else` | `or` |
| `and` | `not` | `true` | `false` |

### Identificadores e literais

| Classe | Descrição |
|---|---|
| `ID` | letra seguida de letras ou dígitos (`contador1`) |
| `INTNUM` | inteiro sem sinal (`42`) |
| `STRING` | cadeia entre aspas simples (`'Resultado: '`) |

### Operadores

| Classe | Lexema | | Classe | Lexema |
|---|---|---|---|---|
| `ASSIGN` | `:=` | | `PLUS` | `+` |
| `EQUAL` | `=` | | `MINUS` | `-` |
| `LESS` | `<` | | `TIMES` | `*` |
| `LESS_EQUAL` | `<=` | | `DIVIDE` | `/` |
| `GREATER` | `>` | | | |
| `GREATER_EQUAL` | `>=` | | | |
| `NOT_EQUAL` | `<>` | | | |

### Delimitadores

| Classe | Lexema | | Classe | Lexema |
|---|---|---|---|---|
| `SEMICOLON` | `;` | | `DOT` | `.` |
| `COMMA` | `,` | | `LEFT_PAREN` | `(` |
| `COLON` | `:` | | `RIGHT_PAREN` | `)` |

E `EOF` para o fim do arquivo. São **45 classes** no total.

## Pontos de atenção

- **Palavras reservadas são case-insensitive.** `Begin`, `begin` e `BEGIN`
  produzem o mesmo token `BEGIN`. É o comportamento do Pascal padrão, e é como a
  apostila escreve os exemplos (`Function`, `Var`, `If ... then`). O lexema
  original é preservado no valor do token, então `Begin` sai como
  `classe=BEGIN, valor=Begin`.
  Repare que é o oposto do analisador de MTL, onde a especificação exige
  comparação exata.
- **Ordem de reconhecimento dos operadores.** O lexema mais longo vence:
  `:=` antes de `:`, `<=` e `<>` antes de `<`, `>=` antes de `>`. Se o analisador
  decidisse pelo primeiro caractere, `:=` viraria `COLON` seguido de `EQUAL` e o
  sintático nunca veria uma atribuição.
- **`(` versus `(*`.** Ao encontrar `(`, o analisador consome o caractere e olha
  o seguinte: se for `*`, é comentário; caso contrário é `LEFT_PAREN`. Por isso
  `(a)` produz três tokens e `(* a *)` não produz nenhum.
- **Aspa dentro de cadeia.** Duas aspas simples seguidas representam uma aspa
  literal: `'nao e''facil'` produz um único `STRING` com o valor `nao e'facil`.
- **Comentários** nas duas formas, `{ ... }` e `(* ... *)`, podendo atravessar
  linhas. Comentário não fechado é erro léxico, reportado na posição de abertura.
- Um identificador não pode começar com dígito: `1a` produz `INTNUM` seguido de
  `ID`, e não um único token.

## Como executar

Pré-requisitos: Java 17 e Maven.

```bash
mvn test
```

```bash
mvn package && java -jar target/pascal-lexer.jar samples/teste.pas
```

Sem argumento, o programa imprime o modo de usar e analisa `samples/teste.pas`.

## Saída

O `samples/teste.pas` foi escrito para exercitar **as 45 classes** e produz 188
tokens. As linhas de comentário (2 e 32) não geram nenhum token.

```
Token [1, 1, classe=PROGRAM, valor=program]
Token [1, 9, classe=ID, valor=exemplo]
Token [1, 16, classe=SEMICOLON, valor=;]
Token [3, 1, classe=VAR, valor=var]
Token [4, 3, classe=ID, valor=x]
Token [4, 4, classe=COMMA, valor=,]
Token [4, 6, classe=ID, valor=y]
Token [4, 8, classe=COLON, valor=:]
```

Atribuição, relacionais e cadeia:

```
Token [9, 9, classe=ASSIGN, valor=:=]
Token [31, 16, classe=GREATER_EQUAL, valor=>=]
Token [37, 25, classe=NOT_EQUAL, valor=<>]
Token [45, 9, classe=LESS_EQUAL, valor=<=]
Token [14, 11, classe=STRING, valor=valores lidos: ]
```

Fim do programa:

```
Token [53, 1, classe=END, valor=end]
Token [53, 4, classe=DOT, valor=.]
Token [54, 1, classe=EOF]
```

Para conferir que o exemplo cobre todas as classes:

```bash
java -jar target/pascal-lexer.jar samples/teste.pas | grep -oE 'classe=[A-Z_]+' | sort -u | wc -l
```

## Erros

| Entrada | Mensagem |
|---|---|
| `x := 5 @ 3` | `Erro léxico: caractere inválido '@' (linha 3, coluna 10)` |
| `x := 'aberta;` | `Erro léxico: cadeia não terminada (linha 3, coluna 8)` |
| `{ sem fim` | `Erro léxico: comentário não terminado (linha 2, coluna 1)` |

Em todos os casos a mensagem vai para `stderr` e o programa encerra com código de
saída 1. Um arquivo válido encerra com 0.

## Organização do código

| Arquivo | Responsabilidade |
|---|---|
| `lexer/SourceReader` | lê o arquivo caractere a caractere e mantém linha e coluna |
| `lexer/TokenType` | as 45 classes de token da linguagem |
| `lexer/TokenValue` | o atributo do token: texto ou inteiro |
| `lexer/Token` | classe, valor e posição |
| `lexer/Lexer` | o autômato: comentários, palavras reservadas, identificadores, inteiros, cadeias e símbolos |
| `lexer/LexicalException` | erro léxico com posição |
| `App` | linha de comando |

O analisador é escrito à mão, sem expressões regulares e sem gerador léxico.

## Fora de escopo

A BNF da disciplina segue com o analisador sintático, a tabela de símbolos e as
ações semânticas de geração de código Assembly 80x86. Este projeto para na
análise léxica.
