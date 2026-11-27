# Fase 1: Construcción (se omiten tests porque en build aún no hay credenciales de Neon)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Fase 2: Ejecución (imagen JRE Alpine liviana sin Maven ni fuentes)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/SoftCorpBackend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Tope de 300 MB de heap para no exceder los 512 MB del plan gratuito de Render
ENV JAVA_OPTS="-Xmx300m -Xss512k"
# sh -c permite expandir $JAVA_OPTS al arrancar el contenedor
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
