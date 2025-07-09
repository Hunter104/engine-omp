FROM openjdk:17-jdk-slim AS build

WORKDIR /app

COPY pom.xml .

COPY common .
COPY kafka-consumer .
COPY kafka-producer .

RUN mvn clean install

FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=build /app/kafka-consumer/target/kafka-consumer.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
