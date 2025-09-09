FROM openjdk:21-slim AS base

FROM base AS builder
WORKDIR /app

COPY . /app
RUN /app/gradlew :discord-app:clean :discord-app:bootJar --no-daemon

FROM base
WORKDIR /app
COPY --from=builder /app/discord-app/build/libs/discord-app-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]

