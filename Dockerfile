# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw \
 && ./mvnw -B -q dependency:go-offline -Dmaven.test.skip=true

COPY src src
RUN ./mvnw -B -q package -Dmaven.test.skip=true

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --create-home appuser
COPY --from=build /app/target/without_backend-*.jar app.jar
USER appuser

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]