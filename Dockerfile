FROM gradle:8.12.1-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test -x check

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/build/libs/SUN_back-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]