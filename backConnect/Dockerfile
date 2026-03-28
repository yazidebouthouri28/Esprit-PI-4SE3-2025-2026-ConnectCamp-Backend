# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

COPY mvnw mvnw.cmd ./
COPY .mvn/ .mvn/
COPY pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring && mkdir -p uploads && chown spring:spring uploads

COPY --from=builder /workspace/target/projet-integre-1.0.0.jar app.jar

USER spring

EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
