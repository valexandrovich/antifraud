FROM openjdk:11
VOLUME /tmp
ARG JAR_FILE=target/*.jar
ARG DB_URL=jdbc:postgresql://localhost:5432/otp
ARG MQ_HOST=localhost
ARG MQ_USER=guest
ARG MQ_PASS=guest
ENV DB_URL=${DB_URL}
ENV MQ_HOST=${MQ_HOST}
ENV MQ_USER=${MQ_USER}
ENV MQ_PASS=${MQ_PASS}
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar","--spring.datasource.url=${DB_URL}","--spring.rabbitmq.username=${MQ_USER}","--spring.rabbitmq.password=${MQ_PASS}","--spring.rabbitmq.host=${MQ_HOST}"]