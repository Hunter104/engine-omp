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
2. app.properties externo
3. app.properties do módulo commons (valores padrão, compilados no jar)

na raíz do projeto há um arquivo app.properties.example com todas as propriedades que podem
ser editadas.

a variável `app.environment` define o ambiente de execução, que pode ser `local` (rodar o mpi
localmente) ou `distributed` (rodar o mpi distribuído com kubernetes).

Propriedades de sistema são passadas ao executar o jar, por exemplo:

```bash
java -Dapp.environment=local -jar kafka-producer/target/kafka-producer.jar
```

Elas podem ser passadas externamente pela variável \_JAVA_OPTIONS
ou diretamente no terminal, por exemplo:

```bash
export _JAVA_OPTIONS="-Dapp.environment=local"
java -jar kafka-producer/target/kafka-producer.jar
```

Suporte para variáveis de ambiente não foi implementado,
pois precisa-se de um mecanismo para conversão de nomes de variáveis
para nomes de propriedades.

## Como executar

Certifique-se de que o Kafka está rodando.
Configurações de host de acordo com o tópico acima.

Certifique-se que o motor de configuração está em PATH (configuração padrão)
ou modifique a properiedade `app.executable` para o path do executável.

**Produtor:**

```bash
java -jar kafka-producer/target/kafka-producer.jar
```

**Consumidor:**

```bash
java -jar kafka-consumer/target/kafka-consumer.jar
```

## Docker

Por enquanto apenas o Dockerfile.gateway está implementado,
que é o gateway feito em java pro kafka, o projeto também
acompanha um arquivo docker-compose.yml (que não está pronto)
para rodar o consumidor e kafka em contêiners.
