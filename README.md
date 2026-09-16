# Analisadores Léxicos e Sintáticos — Compiladores

Trabalhos da disciplina de Compiladores. Quatro analisadores independentes, um
por formato de entrada, escritos em Java sem geradores léxicos, sem geradores
sintáticos e sem expressões regulares: tanto o autômato léxico quanto o parser
recursivo-descendente de cada um são codificados à mão.

| Projeto | Formato | Tokens | Não-terminais | Documentação |
|---|---|---|---|---|
| [`ppm-compiler`](ppm-compiler) | PPM — Portable Pixmap em modo ASCII (`P3`) | 3 | 5 | [README](ppm-compiler/README.md) |
| [`mtl-compiler`](mtl-compiler) | MTL — Material Template Library | 11 | 7 | [README](mtl-compiler/README.md) |
| [`obj-compiler`](obj-compiler) | OBJ — Wavefront, versão texto | 13 | 9 | [README](obj-compiler/README.md) |
| [`pascal-compiler`](pascal-compiler) | Pascal Simplificado (BNF da disciplina) | 45 | 42 | [README](pascal-compiler/README.md) |

Os três primeiros formatos se conectam: um `.obj` referencia um `.mtl` via
`mtllib`, e o `.mtl` referencia a textura — um `.ppm` — via `map_Kd`.

## Pré-requisitos

- Java 17
- Maven

```bash
java -version && mvn -v
```

## Rodar os testes dos quatro projetos

```bash
for project in ppm-compiler mtl-compiler obj-compiler pascal-compiler; do (cd "$project" && mvn -q test) || exit 1; done
```

## Empacotar e executar os quatro

```bash
for project in ppm-compiler mtl-compiler obj-compiler pascal-compiler; do (cd "$project" && mvn -q package && java -jar "target/$project.jar"); done
```

Sem argumento, cada programa analisa o exemplo em sua pasta `samples/`. Para
analisar outro arquivo, passe o caminho:

```bash
cd obj-compiler && java -jar target/obj-compiler.jar caminho/para/modelo.obj
```

## As duas fases

Cada projeto tem duas fases: um analisador léxico (`lexer/Lexer`), que produz
tokens, e um analisador sintático (`parser/Parser`), que consome esses tokens
e reconhece a gramática do formato. O `App` de cada projeto expõe as duas:

```bash
java -jar target/<projeto>.jar <arquivo>            # análise sintática (padrão)
java -jar target/<projeto>.jar <arquivo> --tokens   # apenas os tokens
```

Sem `--tokens`, o `App` monta um `Parser` sobre um `Lexer` e chama `parse()`;
se a entrada é válida, imprime `Análise sintática concluída sem erros:
<arquivo>`. Com `--tokens`, o `App` ignora o `Parser` e imprime cada token do
`Lexer`, um por linha, terminando em `EOF` — o mesmo comportamento que os
quatro projetos tinham quando só existia a fase léxica.

## Anatomia comum

Os quatro projetos têm a mesma estrutura. A duplicação entre eles é deliberada:
cada analisador é um trabalho entregável isoladamente, sem depender de um módulo
compartilhado.

```
<projeto>/
├── pom.xml
├── README.md
├── samples/                     arquivo de exemplo
└── src/
    ├── main/java/comp/<pacote>/
    │   ├── App.java                 linha de comando
    │   ├── CompilationException.java erro comum a léxico e sintático, com linha e coluna
    │   ├── lexer/
    │   │   ├── SourceReader         leitura caractere a caractere, linha e coluna
    │   │   ├── TokenType            as classes de token do formato
    │   │   ├── TokenValue           o atributo do token: texto, inteiro ou decimal
    │   │   ├── Token                classe, valor e posição
    │   │   ├── Lexer                o autômato léxico
    │   │   └── LexicalException     erro léxico com posição
    │   └── parser/
    │       ├── Parser               o parser recursivo-descendente
    │       └── SyntaxException      erro sintático com posição
    └── test/java/comp/<pacote>/
        ├── lexer/
        │   ├── SourceReaderTest
        │   ├── TokenTest
        │   └── LexerTest
        └── parser/
            └── ParserTest
```

`SourceReader`, `Token`, `CompilationException` e as exceções `LexicalException`
e `SyntaxException` são idênticas nos quatro projetos, mudando apenas o pacote.
`TokenValue` declara só as variantes que cada formato usa — PPM e Pascal não
têm literais decimais. `TokenType`, `Lexer` e `Parser` são específicos de cada
formato.

`LexicalException` e `SyntaxException` estendem `CompilationException`, que
guarda linha e coluna e monta a mensagem final. É por isso que existe uma
classe base: todo método do `Parser` declara `throws CompilationException` em
vez de `throws LexicalException, SyntaxException` — no Pascal, por exemplo,
são 46 métodos com `throws`, e sem `CompilationException` cada um deles
listaria as duas subclasses. Mesmo assim, o `App` não sabe, só pela
assinatura, se o erro que pode chegar é léxico (propagado pelo `Lexer` durante
a análise sintática) ou sintático, e por isso captura os dois tipos concretos
antes do tipo base.

## Saída

Todos os analisadores imprimem um token por linha, no mesmo formato:

```
Token [linha, coluna, classe=CLASSE, valor=VALOR]
```

A posição é a do **primeiro caractere do lexema**, contando a partir de 1. Tokens
sem atributo, como `EOF`, omitem o valor:

```
Token [7, 1, classe=EOF]
```

Uma análise sintática bem-sucedida imprime uma única linha:

```
Análise sintática concluída sem erros: samples/teste.pas
```

## Erros

Ao encontrar um erro, o analisador aborta: imprime a mensagem em `stderr` com
linha e coluna e encerra com código de saída 1. O prefixo da mensagem diz de
qual fase o erro veio:

```
Erro léxico: caractere inválido '@' (linha 2, coluna 1)
Erro sintático: faltou ';' depois do nome do programa (linha 2, coluna 1)
```

Um erro léxico pode acontecer mesmo durante a análise sintática — o `Parser`
pede o próximo token ao `Lexer` e não intercepta `LexicalException`, então ela
sobe direto até o `App` com o prefixo `Erro léxico:`, distinto do
`Erro sintático:` que o próprio `Parser` lança.

O `Lexer` lança `LexicalException` e o `Parser` lança `SyntaxException`; quem
decide encerrar o processo é o `App`. Nenhum `System.exit` mora dentro dos
pacotes `lexer` ou `parser`.
