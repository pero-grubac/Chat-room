FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Dependencies first: this layer is reused until pom.xml changes
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests run in CI; the image only needs the jar
RUN mvn -B -q package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# TLS certificate and key come from the Certificate Controller, mounted at /pki
EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
