# Analisadores Léxicos — Compiladores

Trabalhos da disciplina de Compiladores. Quatro analisadores léxicos
independentes, um por formato de entrada, escritos em Java sem geradores léxicos
e sem expressões regulares: o autômato de cada um é codificado à mão.

| Projeto | Formato | Tokens | Documentação |
|---|---|---|---|
| [`ppm-lexer`](ppm-lexer) | PPM — Portable Pixmap em modo ASCII (`P3`) | 3 | [README](ppm-lexer/README.md) |
| [`mtl-lexer`](mtl-lexer) | MTL — Material Template Library | 11 | [README](mtl-lexer/README.md) |
| [`obj-lexer`](obj-lexer) | OBJ — Wavefront, versão texto | 13 | [README](obj-lexer/README.md) |
| [`pascal-lexer`](pascal-lexer) | Pascal Simplificado (BNF da disciplina) | 45 | [README](pascal-lexer/README.md) |

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
for project in ppm-lexer mtl-lexer obj-lexer pascal-lexer; do (cd "$project" && mvn -q test) || exit 1; done
```

## Empacotar e executar os quatro

```bash
for project in ppm-lexer mtl-lexer obj-lexer pascal-lexer; do (cd "$project" && mvn -q package && java -jar "target/$project.jar"); done
```

Sem argumento, cada programa analisa o exemplo em sua pasta `samples/`. Para
analisar outro arquivo, passe o caminho:

```bash
cd obj-lexer && java -jar target/obj-lexer.jar caminho/para/modelo.obj
```

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
    │   ├── App.java             linha de comando
    │   └── lexer/
    │       ├── SourceReader     leitura caractere a caractere, linha e coluna
    │       ├── TokenType        as classes de token do formato
    │       ├── TokenValue       o atributo do token: texto, inteiro ou decimal
    │       ├── Token            classe, valor e posição
    │       ├── Lexer            o autômato
    │       └── LexicalException erro léxico com posição
    └── test/java/comp/<pacote>/lexer/
        ├── SourceReaderTest
        ├── TokenTest
        └── LexerTest
```

`SourceReader`, `Token` e `LexicalException` são idênticos nos quatro projetos,
mudando apenas o pacote. `TokenValue` declara só as variantes que cada formato
usa — PPM e Pascal não têm literais decimais. `TokenType` e `Lexer` são
específicos de cada formato.

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

## Erros léxicos

Ao encontrar um caractere inválido, o analisador aborta: imprime a mensagem em
`stderr` com linha e coluna e encerra com código de saída 1.

```
Erro léxico: caractere inválido '@' (linha 2, coluna 1)
```

O `Lexer` lança `LexicalException`; quem decide encerrar o processo é o `App`.
Nenhum `System.exit` mora dentro do pacote `lexer`, o que mantém o analisador
utilizável por um futuro sintático.
