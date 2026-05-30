# syntax=docker/dockerfile:1.7

# ---------- Stage 1: builder ----------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./

RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src ./src

RUN ./mvnw -B -q package -DskipTests \
    && mv target/*.jar app.jar

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S spring && adduser -S spring -G spring \
    && apk add --no-cache curl

WORKDIR /app

COPY --from=builder --chown=spring:spring /build/app.jar app.jar

USER spring

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
