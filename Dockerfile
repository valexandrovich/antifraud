FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /src
COPY . .
RUN keytool -importcert -file cert.crt -alias ca_certs -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt
RUN mvn -DproxySet=true -DproxyHost=chckproxy.raiffeisenbank.com.ua -DproxyPort=8080 clean package
RUN pwd
RUN ls -la /src/common/target

FROM openjdk:11
WORKDIR /app
COPY --from=build ["/src/common/target/*.jar", "app.jar"]
ENV DB_URL=jdbc:postgresql://localhost:5432/otp
ENV MQ_HOST=localhost
ENV MQ_USER=guest
ENV MQ_PASS=guest
ENTRYPOINT ["java","-jar","app.jar","--spring.datasource.url=${DB_URL}","--spring.rabbitmq.username=${MQ_USER}","--spring.rabbitmq.password=${MQ_PASS}","--spring.rabbitmq.host=${MQ_HOST}"]
