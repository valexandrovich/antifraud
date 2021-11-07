FROM openjdk:11
#ADD . /src
#WORKDIR /src
#RUN ./mvnw package -DskipTests
#
#FROM alpine:3.10.3 as packager
#RUN apk --no-cache add openjdk11-jdk openjdk11-jmods
#ENV JAVA_MINIMAL="/opt/java-minimal"
#RUN /usr/lib/jvm/java-11-openjdk/bin/jlink \
#    --verbose \
#    --add-modules \
#        java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
#    --compress 2 --strip-debug --no-header-files --no-man-pages \
#    --release-info="add:IMPLEMENTOR=dr:IMPLEMENTOR_VERSION=dr_JRE" \
#    --output "$JAVA_MINIMAL"
#
#FROM alpine:3.10.3
#LABEL maintainer="Dmitry Rumyantsev drumyantsev@solidity.com.ua"
#ENV JAVA_HOME=/opt/java-minimal
#ENV PATH="$PATH:$JAVA_HOME/bin"
#COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"
#COPY --from=builder /src/*/target/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","/app.jar"]

VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]
#VOLUME /tmp
#ARG JAR_FILE=common/target/*.jar
#ARG DB_URL=jdbc:postgresql://localhost:5432/otp
#ARG MQ_HOST=localhost
#ARG MQ_USER=guest
#ARG MQ_PASS=guest
#ENV DB_URL=${DB_URL}
#ENV MQ_HOST=${MQ_HOST}
#ENV MQ_USER=${MQ_USER}
#ENV MQ_PASS=${MQ_PASS}
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java","-jar","/app.jar","--spring.datasource.url=${DB_URL}","--spring.rabbitmq.username=${MQ_USER}","--spring.rabbitmq.password=${MQ_PASS}","--spring.rabbitmq.host=${MQ_HOST}"]