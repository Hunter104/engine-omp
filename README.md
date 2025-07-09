# Motor de Jogo da Vida Híbrido

Diretório raíz: `./engine`

para compilar:

```bash
make all
```

Target irá gerar o executável (baseado em mpi) em `./build/gol-hybrid`

# Produtor e Consumidor Kafka (CLI)

diretório raíz: `./kafka-apps`

- `common/`: código compartilhado (mensagens, constantes, etc.)
- `kafka-producer/`: produtor interativo via terminal
- `kafka-consumer/`: consumidor que roda jogo da vida híbrido

## Como compilar

No diretório raiz do projeto, execute:

```bash
mvn clean install
```

Obs: precisa ter o maven instalado

## Configuração

A prioridade de configuração é, em ordem:

1. Propriedades de sistema/jvm
2. Variáveis de ambiente
3. config.properties do módulo commons

na raíz do projeto há um arquivo .env.example para demonstração de configuração

## Como executar

Certifique-se de que o Kafka está rodando.
Configurações de host e tal estão no módulo common.

Certifique-se que o motor foi compilaod e o executável está em PATH,
OU que o caminho do executável está configurado na variável de ambiente `app.executable`,
ou em outras formas de configuração listadas acima

**Produtor:**

```bash
java -jar kafka-producer/target/kafka-producer.jar
```

**Consumidor:**

```bash
java -jar kafka-consumer/target/kafka-consumer.jar
```

