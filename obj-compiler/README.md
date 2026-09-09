# Analisador Léxico — OBJ (Wavefront)

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
mvn package && java -jar target/obj-lexer.jar samples/cube.obj
```

Sem argumento, o programa imprime o modo de usar e analisa `samples/cube.obj`.

## Saída

O `samples/cube.obj` produz **297 tokens**. As linhas 1 a 15 são comentários e
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
| `App` | linha de comando |

O analisador é escrito à mão, sem expressões regulares e sem gerador léxico.
