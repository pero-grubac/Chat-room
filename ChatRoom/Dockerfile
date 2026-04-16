FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mkdir -p src/main/resources/certs && \
    keytool -genkeypair \
      -alias chatroom \
      -keyalg RSA \
      -keysize 2048 \
      -storetype PKCS12 \
      -keystore src/main/resources/certs/keystore.p12 \
      -validity 3650 \
      -storepass ${SSL_KEY_STORE_PASSWORD:-chatroom123} \
      -keypass ${SSL_KEY_PASSWORD:-chatroom123} \
      -dname "CN=localhost, OU=Dev, O=ChatRoom, L=BanjaLuka, S=RS, C=BA" \
      -noprompt

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
