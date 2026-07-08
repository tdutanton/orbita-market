# Build stage
FROM gradle:8.14.1-jdk21 AS builder
WORKDIR /app

# Кэш зависимости
COPY build.gradle.kts settings.gradle.kts ./
COPY --chown=gradle:gradle . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN gradle dependencies --refresh-dependencies && \
    gradle build -x test --no-daemon


# Run stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app

# Создание непривилегированного пользователя
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Копируются только JAR из builder
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# Открытие порта
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Запуск от непривилегированного пользователя
USER appuser

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]