FROM gradle:8.12.1-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test -x check

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar diassist.jar
ENTRYPOINT ["java", "-jar", "SUN_back.jar"]