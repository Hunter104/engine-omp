# Produtor e Consumidor Kafka (CLI)

* `common/`: código compartilhado (mensagens, constantes, etc.)
* `kafka-producer/`: produtor interativo via terminal
* `kafka-consumer/`: consumidor que roda jogo da vida híbrido

## Como compilar

No diretório raiz do projeto, execute:

```bash
mvn clean install
```

Obs: precisa ter o maven instalado

## Como executar

Certifique-se de que o Kafka está rodando.
Configurações de host e tal estão no módulo common.

**Produtor:**

```bash
java -jar kafka-producer/target/kafka-producer.jar
```

**Consumidor:**

```bash
java -jar kafka-consumer/target/kafka-consumer.jar
```