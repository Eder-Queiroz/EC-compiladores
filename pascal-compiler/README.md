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
mvn package && java -jar target/pascal-compiler.jar samples/teste.pas
```

Sem argumento, o programa imprime o modo de usar e analisa `samples/teste.pas`.

## Saída

O `samples/teste.pas` é o programa Fibonacci: lê quantos termos (`quantos`) o
usuário quer ver e calcula a sequência com `termo1`, `termo2`, `aux` e `cont`.
Ele não tem comentários nem declara sub-rotinas. Produz **100 tokens** e cobre
23 das 45 classes — não usa, por exemplo, `PROCEDURE`, `FUNCTION`, `FOR`,
`GREATER`, `GREATER_EQUAL`, `NOT_EQUAL` ou `LESS_EQUAL`.

```
Token [1, 1, classe=PROGRAM, valor=Program]
Token [1, 9, classe=ID, valor=Fibonacci]
Token [1, 18, classe=SEMICOLON, valor=;]
Token [2, 1, classe=VAR, valor=Var]
Token [2, 5, classe=ID, valor=termo1]
Token [2, 11, classe=COMMA, valor=,]
Token [2, 13, classe=ID, valor=termo2]
Token [2, 40, classe=COLON, valor=:]
```

Atribuição, relacional e cadeia:

```
Token [5, 12, classe=ASSIGN, valor=:=]
Token [14, 17, classe=LESS, valor=<]
Token [4, 13, classe=STRING, valor=----------FIBONACCI------------]
```

Fim do programa:

```
Token [21, 5, classe=END, valor=end]
Token [22, 1, classe=END, valor=End]
Token [22, 4, classe=DOT, valor=.]
Token [23, 1, classe=EOF]
```

Para contar quantas classes distintas o exemplo cobre:

```bash
java -jar target/pascal-compiler.jar samples/teste.pas --tokens | grep -oE 'classe=[A-Z_]+' | sort -u | wc -l
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

## Análise sintática

Gramática do analisador recursivo-descendente, já com as fatorações descritas
abaixo:

```
<programa>          ::= program id ; <corpo> .
<corpo>              ::= <declara> <rotina> begin <sentencas> end
<declara>            ::= var <dvar> <mais_dc> | ε
<mais_dc>            ::= ; <cont_dc>
<cont_dc>            ::= <dvar> <mais_dc> | ε
<dvar>               ::= <variaveis> : <tipo_var>
<tipo_var>           ::= integer
<variaveis>          ::= id <mais_var>
<mais_var>           ::= , <variaveis> | ε
<rotina>             ::= <procedimento> | <funcao> | ε
<procedimento>       ::= procedure id <parametros> ; <corpo> ; <rotina>
<funcao>             ::= function id <parametros> : <tipo_funcao> ; <corpo> ; <rotina>
<tipo_funcao>        ::= integer
<parametros>         ::= ( <lista_parametros> ) | ε
<lista_parametros>   ::= <lista_id> : <tipo_var> <cont_lista_par>
<cont_lista_par>     ::= ; <lista_parametros> | ε
<lista_id>           ::= id <cont_lista_id>
<cont_lista_id>      ::= , <lista_id> | ε
<sentencas>          ::= <comando> <mais_sentencas>
<mais_sentencas>     ::= ; <cont_sentencas>
<cont_sentencas>     ::= <sentencas> | ε
<comando>            ::= if ( <expressao_logica> ) then begin <sentencas> end <pfalsa>
                       | while ( <expressao_logica> ) do begin <sentencas> end
                       | repeat <sentencas> until ( <expressao_logica> )
                       | read ( <var_read> )
                       | write ( <exp_write> )
                       | writeln ( <exp_write> )
                       | for id := <expressao> to <expressao> do begin <sentencas> end
                       | id <command_tail>
<command_tail>       ::= := <expressao> | <argumentos>
<pfalsa>             ::= else begin <sentencas> end | ε
<var_read>           ::= id <mais_var_read>
<mais_var_read>      ::= , <var_read> | ε
<exp_write>          ::= id <mais_exp_write> | string <mais_exp_write> | intnum <mais_exp_write>
<mais_exp_write>     ::= , <exp_write> | ε
<argumentos>         ::= ( <lista_arg> ) | ε
<lista_arg>          ::= <expressao> <cont_lista_arg>
<cont_lista_arg>     ::= , <lista_arg> | ε
<expressao_logica>   ::= <termo_logico> <mais_expr_logica>
<mais_expr_logica>   ::= or <termo_logico> <mais_expr_logica> | ε
<termo_logico>       ::= <fator_logico> <mais_termo_logico>
<mais_termo_logico>  ::= and <fator_logico> <mais_termo_logico> | ε
<fator_logico>       ::= ( <expressao_logica> ) | not <fator_logico> | true | false | <relacional>
<relacional>         ::= <expressao> = <expressao>
                       | <expressao> > <expressao>
                       | <expressao> >= <expressao>
                       | <expressao> < <expressao>
                       | <expressao> <= <expressao>
                       | <expressao> <> <expressao>
<expressao>          ::= <termo> <mais_expressao>
<mais_expressao>     ::= + <termo> <mais_expressao> | - <termo> <mais_expressao> | ε
<termo>              ::= <fator> <mais_termo>
<mais_termo>         ::= * <fator> <mais_termo> | / <fator> <mais_termo> | ε
<fator>              ::= id <argumentos> | intnum | ( <expressao> )
```

### Mapeamento de não-terminais para métodos

Com 42 não-terminais, a tabela abaixo é o substituto acordado para comentários
no código — cada método corresponde a exatamente uma produção da gramática
acima.

| Não-terminal | Método |
|---|---|
| `<programa>` | `program()` |
| `<corpo>` | `body()` |
| `<declara>` | `declarations()` |
| `<mais_dc>` | `moreDeclarations()` |
| `<cont_dc>` | `declarationsTail()` |
| `<dvar>` | `variableDeclaration()` |
| `<tipo_var>` | `variableType()` |
| `<variaveis>` | `variables()` |
| `<mais_var>` | `moreVariables()` |
| `<rotina>` | `routine()` |
| `<procedimento>` | `procedureDeclaration()` |
| `<funcao>` | `functionDeclaration()` |
| `<parametros>` | `parameters()` |
| `<lista_parametros>` | `parameterList()` |
| `<cont_lista_par>` | `parameterListTail()` |
| `<lista_id>` | `identifierList()` |
| `<cont_lista_id>` | `identifierListTail()` |
| `<tipo_funcao>` | `functionType()` |
| `<sentencas>` | `statements()` |
| `<mais_sentencas>` | `moreStatements()` |
| `<cont_sentencas>` | `statementsTail()` |
| `<var_read>` | `readArguments()` |
| `<mais_var_read>` | `moreReadArguments()` |
| `<exp_write>` | `writeArguments()` |
| `<mais_exp_write>` | `moreWriteArguments()` |
| `<comando>` | `statement()` |
| `<command_tail>` (fatorado) | `statementTail()` |
| `<pfalsa>` | `elsePart()` |
| `<argumentos>` | `arguments()` |
| `<lista_arg>` | `argumentList()` |
| `<cont_lista_arg>` | `argumentListTail()` |
| `<expressao_logica>` | `logicalExpression()` |
| `<mais_expr_logica>` | `moreLogicalExpression()` |
| `<termo_logico>` | `logicalTerm()` |
| `<mais_termo_logico>` | `moreLogicalTerm()` |
| `<fator_logico>` | `logicalFactor()` |
| `<relacional>` | `relational()` |
| `<expressao>` | `expression()` |
| `<mais_expressao>` | `moreExpression()` |
| `<termo>` | `term()` |
| `<mais_termo>` | `moreTerm()` |
| `<fator>` | `factor()` |

`<chamada_procedimento>` não aparece na tabela — foi absorvido pela fatoração
de `<comando>` descrita a seguir.

### As três fatorações

1. **`<comando> ::= id <command_tail>`.** A BNF original tinha
   `<comando> ::= <atribuicao> | <chamada_procedimento>`, ambos começando por
   `id`, e só o token seguinte (`:=` ou `(`/`;`) diria qual dos dois é. Isso
   exigiria dois tokens de lookahead. Fatorando, o parser consome o `id` uma
   única vez e decide em `statementTail()`: `:=` vira atribuição,
   qualquer outra coisa (inclusive nada) vira `<argumentos>`, ou seja, chamada
   de procedimento.
2. **`<fator> ::= id <argumentos> | intnum | ( <expressao> )`.** A BNF
   original listava `id` duas vezes — uma para variável, outra para chamada de
   função dentro de expressão (`id ( <lista_arg> )`). Como `<argumentos>` pode
   derivar `ε`, uma única alternativa `id <argumentos>` cobre os dois casos:
   sem parênteses é variável, com parênteses é chamada de função.
3. **`<relacional>`** é fatorado à esquerda: em vez de seis alternativas que
   cada uma repetiria `<expressao> <expressao>` com um operador diferente no
   meio, `relational()` chama `expression()` uma única vez e depois exige um
   dos seis operadores (`=`, `>`, `>=`, `<`, `<=`, `<>`), seguido da segunda
   `<expressao>`. Sem isso, o parser precisaria calcular a primeira
   `<expressao>` seis vezes (uma por alternativa) antes de saber qual operador
   viria.

### Ambiguidade não resolvida

Em `<fator_logico>`, o token `(` pode iniciar tanto
`( <expressao_logica> )` quanto um `<relacional>` cuja `<expressao>` da
esquerda começa com `(` (por exemplo, `(a + b) > c`). `logicalFactor()` sempre
tenta a primeira alternativa quando vê `(`. O custo é explícito e documentado,
não escondido:

- `if ((a > b) and (c < d)) then` **funciona** — o `(` externo abre um
  `<fator_logico>` que é `( <expressao_logica> )`, e dentro dele `a > b` e
  `c < d` são `<relacional>` que não começam com `(`.
- `if ((a + b) > c) then` **é rejeitado** — o `(` externo é consumido como
  início de `( <expressao_logica> )`, o parser tenta interpretar
  `(a + b) > c)` como uma expressão lógica, e a gramática não tem uma
  alternativa que produza isso a partir daí.

### Duas observações da gramática

- **Todo comando termina com `;`, inclusive o último antes de `end`.**
  `<mais_sentencas> ::= ; <cont_sentencas>` não tem alternativa `ε`, então o
  `;` depois do comando é obrigatório — `moreStatements()` chama
  `expect(SEMICOLON, ...)` incondicionalmente, e só depois verifica
  (`statementsTail()`) se há mais um comando. Isso é diferente do Pascal
  padrão, onde o `;` antes de `end` é opcional.
- **`<exp_write>` aceita `id`, `string` ou `intnum`, não uma expressão
  completa.** `writeArguments()` chama `accept(ID)`, `accept(STRING)` ou
  `accept(INTNUM)` diretamente, sem passar por `expression()`. Por isso
  `write(x + 1)` é rejeitado: o parser vê `x`, aceita como argumento, e então
  espera `,` ou `)` — o `+` que vem em seguida é um erro sintático.

### Modos de execução

1. **Análise sintática completa** (padrão):
   ```bash
   java -jar target/pascal-compiler.jar samples/teste.pas
   ```
   Analisa o arquivo especificado e imprime sucesso ou erro.

2. **Modo tokens** (apenas léxico):
   ```bash
   java -jar target/pascal-compiler.jar samples/teste.pas --tokens
   ```
   Analisa e lista todos os tokens do arquivo.

### Saída real dos quatro comandos

Análise bem-sucedida do exemplo padrão:
```bash
$ java -jar target/pascal-compiler.jar samples/teste.pas
Análise sintática concluída sem erros: samples/teste.pas
```

Modo tokens, última linha:
```bash
$ java -jar target/pascal-compiler.jar samples/teste.pas --tokens | tail -1
Token [23, 1, classe=EOF]
```

Erro sintático (`program p` sem `;`, com `begin end.` logo depois):
```bash
$ printf 'program p\nbegin\nend.\n' > /tmp/erro.pas
$ java -jar target/pascal-compiler.jar /tmp/erro.pas; echo "exit=$?"
Erro sintático: faltou ';' depois do nome do programa (linha 2, coluna 1)
exit=1
```

Erro léxico durante a análise sintática (`@` não é um caractere válido):
```bash
$ printf 'program p;\nbegin\n  x := @;\nend.\n' > /tmp/lex.pas
$ java -jar target/pascal-compiler.jar /tmp/lex.pas; echo "exit=$?"
Erro léxico: caractere inválido '@' (linha 3, coluna 8)
exit=1
```

O quarto comando comprova que os dois prefixos de erro (`Erro sintático:` e
`Erro léxico:`) são distintos e que o erro léxico continua sendo detectado
mesmo durante a análise sintática — o `Parser` delega a tokenização ao
`Lexer` e não intercepta `LexicalException`.
