# Compilador MTL (Material Template Library)

Reconhece os itens léxicos de arquivos `.mtl`, o "arquivo de cabeçalho" de
propriedades visuais de um `.obj`. O `.obj` não guarda a imagem da textura: ele
guarda o mapeamento UV, e é o `.mtl` que informa o nome do arquivo de imagem.

```mtl
newmtl texture
Ka 0.0 0.0 0.0
Kd 0.5 0.5 0.5
Ks 0.0 0.0 0.0
Ns 10.0
illum 2
map_Kd texture.ppm
```

## Tokens

| Classe | Descrição |
|---|---|
| `KW_NEWMTL` | a cadeia exata `newmtl` |
| `KW_KA` | a cadeia exata `Ka` — cor ambiente |
| `KW_KD` | a cadeia exata `Kd` — cor difusa |
| `KW_KS` | a cadeia exata `Ks` — cor especular |
| `KW_NS` | a cadeia exata `Ns` — expoente especular (brilho) |
| `KW_ILLUM` | a cadeia exata `illum` — modelo de iluminação |
| `KW_MAP_KD` | a cadeia exata `map_Kd` — arquivo de textura |
| `INTEIRO` | número inteiro, necessário para o parâmetro de `illum` |
| `FLOAT` | número decimal (`1.0`, `0.5`) |
| `IDENTIFICADOR` | nome do material ou do arquivo (`MatMadeira`, `texture.ppm`) |
| `EOF` | fim do arquivo |

## Pontos de atenção

- **As palavras reservadas são case-sensitive.** A especificação diz "a cadeia
  exata", então `Kd` é palavra reservada e `kd` é `IDENTIFICADOR`. É diferente do
  Pascal, onde as palavras reservadas ignoram maiúsculas.
- **`texture.ppm` é um único `IDENTIFICADOR`**, não identificador + ponto +
  identificador. O identificador começa com letra ou `_` e continua aceitando
  letra, dígito, `_`, `-` e `.`. Como o ponto só entra no meio de uma palavra que
  já começou com letra, não há conflito com `FLOAT`.
- **`FLOAT` vs `INTEIRO`:** o ponto decide. `10.0` é `FLOAT`, `2` é `INTEIRO`.
- `#` inicia comentário até o fim da linha.

## Como executar

Pré-requisitos: Java 17 e Maven.

```bash
mvn test
```

```bash
mvn package && java -jar target/mtl-compiler.jar samples/cube.mtl
```

Sem argumento, o programa imprime o modo de usar e analisa `samples/cube.mtl`. O programa oferece dois modos: análise sintática completa (modo padrão) ou apenas listagem de tokens (modo `--tokens`).

## Saída

```
Token [1, 1, classe=KW_NEWMTL, valor=newmtl]
Token [1, 8, classe=IDENTIFICADOR, valor=texture]
Token [2, 1, classe=KW_KA, valor=Ka]
Token [2, 4, classe=FLOAT, valor=0.0]
Token [2, 8, classe=FLOAT, valor=0.0]
Token [2, 12, classe=FLOAT, valor=0.0]
Token [3, 1, classe=KW_KD, valor=Kd]
Token [3, 4, classe=FLOAT, valor=0.5]
Token [3, 8, classe=FLOAT, valor=0.5]
Token [3, 12, classe=FLOAT, valor=0.5]
Token [4, 1, classe=KW_KS, valor=Ks]
Token [4, 4, classe=FLOAT, valor=0.0]
Token [4, 8, classe=FLOAT, valor=0.0]
Token [4, 12, classe=FLOAT, valor=0.0]
Token [5, 1, classe=KW_NS, valor=Ns]
Token [5, 4, classe=FLOAT, valor=10.0]
Token [6, 1, classe=KW_ILLUM, valor=illum]
Token [6, 7, classe=INTEIRO, valor=2]
Token [7, 1, classe=KW_MAP_KD, valor=map_Kd]
Token [7, 8, classe=IDENTIFICADOR, valor=texture.ppm]
Token [8, 1, classe=EOF]
```

## Erros

Caractere inválido é reportado em `stderr` com linha e coluna, e o programa
encerra com código de saída 1.

```
Erro léxico: caractere inválido '$' (linha 2, coluna 1)
```

## Organização do código

| Arquivo | Responsabilidade |
|---|---|
| `lexer/SourceReader` | lê o arquivo caractere a caractere e mantém linha e coluna |
| `lexer/TokenType` | as 11 classes de token do formato |
| `lexer/TokenValue` | o atributo do token: texto, inteiro ou decimal |
| `lexer/Token` | classe, valor e posição |
| `lexer/Lexer` | o autômato: separadores, comentários, palavras reservadas e números |
| `lexer/LexicalException` | erro léxico com posição |
| `parser/Parser` | analisador sintático recursivo descendente |
| `parser/SyntaxException` | erro sintático com posição |
| `App` | linha de comando |

O analisador léxico é escrito à mão, sem expressões regulares e sem gerador léxico.

## Análise sintática

A segunda fase do compilador implementa um analisador sintático recursivo descendente que valida a estrutura de um arquivo `.mtl` segundo a seguinte gramática:

```
<material_library> ::= <materials>
<materials>        ::= <material> <materials> | ε
<material>         ::= KW_NEWMTL IDENTIFICADOR <properties>
<properties>       ::= <property> <properties> | ε
<property>         ::= KW_KA <color> | KW_KD <color> | KW_KS <color>
                     | KW_NS FLOAT | KW_ILLUM INTEIRO | KW_MAP_KD IDENTIFICADOR
<color>            ::= FLOAT FLOAT FLOAT
```

### Mapeamento entre não-terminais e métodos

| Não-terminal | Método |
|---|---|
| `<material_library>` | `materialLibrary()` |
| `<materials>` | `materials()` |
| `<material>` | `material()` |
| `<properties>` | `properties()` |
| `<property>` | `property()` |
| `<color>` | `color()` |

### Semântica

As propriedades de um material (`Ka`, `Kd`, `Ks`, `Ns`, `illum`, `map_Kd`) podem aparecer em qualquer ordem e ser repetidas, pois o formato MTL não impõe restrições a respeito.

**`FLOAT` estrito nas cores e no expoente.** `Ka`, `Kd`, `Ks` exigem três `FLOAT`
e `Ns` exige um `FLOAT` — `Kd 1 0 0` é erro sintático, porque o léxico classifica
`1` como `INTEIRO`. É o que a gramática do enunciado pede
(`<cor_difusa> ::= KW_KD FLOAT FLOAT FLOAT`). Só `illum` recebe `INTEIRO`, por ser
um índice de modelo de iluminação. O `samples/cube.mtl` do enunciado já escreve
todos os valores com ponto decimal, então a exigência não conflita com ele.

### Iteração em vez de recursão

A gramática acima escreve `<materials>` e `<properties>` como recursão à
direita, mas `materials()` e `properties()` são implementados com laços
`while`, não com chamada recursiva a si mesmos. Um arquivo `.mtl` pode
declarar um número arbitrário de materiais e propriedades, e recursão própria
consumiria um quadro de pilha por repetição — um `StackOverflowError` certo
em uma biblioteca de materiais grande. O laço aceita exatamente a mesma
linguagem, já que iteração é a transformação padrão de uma recursão de
cauda. O analisador de Pascal faz o oposto de propósito: lá a repetição
segue a estrutura do programa, então a recursão própria é natural.

### Modos de execução

**Modo análise sintática (padrão):**

```bash
java -jar target/mtl-compiler.jar <arquivo>
```

Analisa o arquivo e reporta sucesso ou erro sintático.

**Modo apenas tokens:**

```bash
java -jar target/mtl-compiler.jar <arquivo> --tokens
```

Lista todos os tokens do arquivo sem realizar análise sintática.

### Exemplos de saída

Sucesso — análise completa:

```
Análise sintática concluída sem erros: samples/cube.mtl
```

Modo tokens — último token (EOF):

```
Token [8, 1, classe=EOF]
```

Erro — propriedade incompleta:

```
Erro sintático: faltou a componente azul da cor (linha 3, coluna 1)
```
