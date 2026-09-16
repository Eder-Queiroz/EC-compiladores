# Compilador OBJ (Wavefront)

Reconhece os itens léxicos de arquivos `.obj` em sua versão texto/ASCII, o
formato de objeto 3D mais simples de se trabalhar. O `.obj` não guarda a imagem
da textura: guarda o mapeamento UV (coordenadas de textura) e referencia um
arquivo `.mtl` via `mtllib`, que por sua vez aponta a imagem.

## Tokens

| Classe | Descrição |
|---|---|
| `KW_MTLLIB` | a cadeia exata `mtllib` |
| `KW_USEMTL` | a cadeia exata `usemtl` |
| `KW_V` | a cadeia exata `v` — vértice espacial |
| `KW_VT` | a cadeia exata `vt` — vértice de textura (UV) |
| `KW_VN` | a cadeia exata `vn` — vetor normal |
| `KW_F` | a cadeia exata `f` — face/polígono |
| `KW_G` | a cadeia exata `g` — declaração de grupo |
| `KW_O` | a cadeia exata `o` — declaração de objeto |
| `BARRA` | o caractere exato `/` |
| `FLOAT` | número decimal, podendo ser negativo (`-0.5`, `1.0`) |
| `INTEIRO` | número inteiro positivo, índice (`1`, `2`, `3`) |
| `IDENTIFICADOR` | nome de arquivo ou material (`cube.mtl`, `texture`) |
| `EOF` | fim do arquivo |

## Pontos de atenção

- **Maximal munch.** `vt` e `vn` precisam casar antes de `v`. O analisador lê o
  lexema inteiro até o delimitador e só então consulta a tabela de palavras
  reservadas — nunca decide pelo primeiro caractere. É por isso que `vn` não vira
  `KW_V` seguido de um identificador `n`.
- **Sinal negativo** só aparece em `FLOAT` (`vn -1.0 0.0 0.0`). Índices de face e
  valores de textura são positivos.
- **`FLOAT` vs `INTEIRO`:** o ponto decide. Em `vt 0 0.75`, o `0` é `INTEIRO` e o
  `0.75` é `FLOAT`.
- **Normalização do valor decimal.** O lexema `1.00` produz `valor=1.0`. O token
  carrega o *valor* numérico convertido, não a cadeia bruta — é o atributo do
  token que a apostila descreve ao lado da classe.
- **`cube.mtl` é um único `IDENTIFICADOR`.** O identificador começa com letra ou
  `_` e continua aceitando letra, dígito, `_`, `-` e `.`. Como o ponto só entra
  no meio de uma palavra iniciada por letra, não há conflito com `FLOAT`.
- **Faces:** `f 3/10/1` produz `KW_F INTEIRO BARRA INTEIRO BARRA INTEIRO`.
  Verificar se o índice existe é papel do analisador sintático, não do léxico.
- `#` inicia comentário até o fim da linha, inclusive no fim de uma linha com
  conteúdo (`v 0.0 0.0 0.0  # 1 a`).

## Como executar

Pré-requisitos: Java 17 e Maven.

```bash
mvn test
```

```bash
mvn package && java -jar target/obj-compiler.jar samples/cube.obj
```

Sem argumento, o programa imprime o modo de usar e analisa `samples/cube.obj`.

## Saída

O modo padrão (análise sintática) imprime uma linha de sucesso ou o erro
encontrado:

```
Análise sintática concluída sem erros: samples/cube.obj
```

Com `--tokens`, o programa lista todos os tokens do arquivo, um por linha. O
`samples/cube.obj` produz **297 tokens**. As linhas 1 a 15 são comentários e
não geram nenhum token — o primeiro token está na linha 16.

```
Token [16, 1, classe=KW_MTLLIB, valor=mtllib]
Token [16, 8, classe=IDENTIFICADOR, valor=cube.mtl]
Token [18, 1, classe=KW_G, valor=g]
Token [18, 3, classe=IDENTIFICADOR, valor=cube]
Token [21, 1, classe=KW_V, valor=v]
Token [21, 3, classe=FLOAT, valor=0.0]
Token [21, 7, classe=FLOAT, valor=0.0]
Token [21, 11, classe=FLOAT, valor=0.0]
```

Normal com componente negativa (linha 33, `vn -1.0  0.0  0.0`):

```
Token [33, 1, classe=KW_VN, valor=vn]
Token [33, 4, classe=FLOAT, valor=-1.0]
Token [33, 10, classe=FLOAT, valor=0.0]
Token [33, 15, classe=FLOAT, valor=0.0]
```

Coordenada de textura misturando inteiro e decimal (linha 43, `vt 0    0.75`):

```
Token [43, 1, classe=KW_VT, valor=vt]
Token [43, 4, classe=INTEIRO, valor=0]
Token [43, 9, classe=FLOAT, valor=0.75]
```

Face (linha 67, `f 3/10/1 7/6/1 8/5/1`) — 16 tokens:

```
Token [67, 1, classe=KW_F, valor=f]
Token [67, 3, classe=INTEIRO, valor=3]
Token [67, 4, classe=BARRA, valor=/]
Token [67, 5, classe=INTEIRO, valor=10]
Token [67, 7, classe=BARRA, valor=/]
Token [67, 8, classe=INTEIRO, valor=1]
Token [67, 10, classe=INTEIRO, valor=7]
Token [67, 11, classe=BARRA, valor=/]
Token [67, 12, classe=INTEIRO, valor=6]
Token [67, 13, classe=BARRA, valor=/]
Token [67, 14, classe=INTEIRO, valor=1]
Token [67, 16, classe=INTEIRO, valor=8]
Token [67, 17, classe=BARRA, valor=/]
Token [67, 18, classe=INTEIRO, valor=5]
Token [67, 19, classe=BARRA, valor=/]
Token [67, 20, classe=INTEIRO, valor=1]
```

## Erros

| Entrada | Mensagem |
|---|---|
| `%` | `Erro léxico: caractere inválido '%' (linha 2, coluna 1)` |
| `vn -` | `Erro léxico: número mal formado '-' (linha 1, coluna 4)` |
| `vt .` | `Erro léxico: número mal formado '.' (linha 1, coluna 4)` |

Em todos os casos a mensagem vai para `stderr` e o programa encerra com código de
saída 1.

## Organização do código

| Arquivo | Responsabilidade |
|---|---|
| `lexer/SourceReader` | lê o arquivo caractere a caractere e mantém linha e coluna |
| `lexer/TokenType` | as 13 classes de token do formato |
| `lexer/TokenValue` | o atributo do token: texto, inteiro ou decimal |
| `lexer/Token` | classe, valor e posição |
| `lexer/Lexer` | o autômato: separadores, comentários, palavras reservadas, números e barra |
| `lexer/LexicalException` | erro léxico com posição |
| `CompilationException` | superclasse abstrata dos erros de compilação, guarda linha e coluna |
| `parser/Parser` | analisador sintático recursivo descendente |
| `parser/SyntaxException` | erro sintático com posição |
| `App` | linha de comando: analisa por padrão, lista os tokens com `--tokens` |

O analisador é escrito à mão, sem expressões regulares e sem gerador léxico.

## Análise sintática

Gramática do analisador recursivo-descendente:

```
<model>             ::= <commands>
<commands>          ::= <command> <commands> | ε
<command>           ::= KW_MTLLIB IDENTIFICADOR | KW_USEMTL IDENTIFICADOR
                      | KW_G IDENTIFICADOR | KW_O IDENTIFICADOR
                      | KW_V <number> <number> <number>
                      | KW_VN <number> <number> <number>
                      | KW_VT <number> <number>
                      | KW_F <vertex> <vertex> <vertex> <more_vertices>
<more_vertices>     ::= <vertex> <more_vertices> | ε
<vertex>            ::= INTEIRO <references>
<references>        ::= BARRA <texture_reference> | ε
<texture_reference> ::= INTEIRO <normal_reference> | BARRA INTEIRO
<normal_reference>  ::= BARRA INTEIRO | ε
<number>            ::= INTEIRO | FLOAT
```

### Mapeamento de não-terminais para métodos

| Não-terminal | Método |
|---|---|
| `<model>` | `model()` |
| `<commands>` | `commands()` |
| `<command>` | `command()` |
| `<more_vertices>` | `moreVertices()` |
| `<vertex>` | `vertex()` |
| `<references>` | `references()` |
| `<texture_reference>` | `textureReference()` |
| `<normal_reference>` | `normalReference()` |
| `<number>` | `number(String)` |

### Formas de referência de vértice

Faces em OBJ aceitam quatro formas de referência de vértice:

| Forma | Derivação | Exemplo |
|---|---|---|
| Vértice apenas | `INTEIRO` | `f 3 7 8` |
| Vértice + textura | `INTEIRO BARRA INTEIRO` | `f 3/10 7/6 8/5` |
| Vértice + normal | `INTEIRO BARRA BARRA INTEIRO` | `f 3//1 7//1 8//1` |
| Vértice + textura + normal | `INTEIRO BARRA INTEIRO BARRA INTEIRO` | `f 3/10/1 7/6/1 8/5/1` |

### Limitações assumidas

- Vértices com quarto componente (forma `v x y z w`) não são aceitos.
- Coordenadas de textura com terceiro componente (forma `vt u v w`) não são aceitas.

### Iteração em vez de recursão

A gramática acima escreve `<commands>` e `<more_vertices>` como recursão à
direita, mas `commands()` e `moreVertices()` são implementados com laços
`while`, não com chamada recursiva a si mesmos. Um modelo `.obj` pode ter
milhares de comandos (vértices, normais, coordenadas de textura, faces), e
recursão própria consumiria um quadro de pilha por repetição — um
`StackOverflowError` certo em qualquer modelo grande. O laço aceita
exatamente a mesma linguagem, já que iteração é a transformação padrão de uma
recursão de cauda. O analisador de Pascal faz o oposto de propósito: lá a
repetição segue a estrutura do programa, então a recursão própria é natural.

### Modos de execução

1. **Análise sintática completa** (padrão):
   ```bash
   java -jar target/obj-compiler.jar samples/cube.obj
   ```
   Analisa o arquivo especificado e imprime sucesso ou erro.

2. **Modo tokens** (apenas léxico):
   ```bash
   java -jar target/obj-compiler.jar samples/cube.obj --tokens
   ```
   Lista todos os tokens do arquivo sem realizar análise sintática.

### Saída

Análise bem-sucedida:
```
Análise sintática concluída sem erros: samples/cube.obj
```

Listagem de tokens (final do arquivo):
```
Token [89, 1, classe=EOF]
```

Erro sintático (arquivo com apenas dois vértices de face):
```
Erro sintático: faltou o índice do vértice na face (linha 2, coluna 1)
exit=1
```
