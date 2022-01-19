FROM openjdk:11-jre
<<<<<<< HEAD
ENV JAVA_OPTS="-Dspring.datasource.url=jdbc:postgresql://antifrauddbdev.raiffeisenbank.com.ua:5432/otp -Dspring.rabbitmq.host=rabbitmq.antifraud-deps.svc.cluster.local -Dspring.rabbitmq.username=guest -Dspring.rabbitmq.password=guest"
ENV OTP_TEMP=/tmp
=======

ENV JAVA_OPTS="-Dspring.datasource.url=jdbc:postgresql://antifrauddbdev.raiffeisenbank.com.ua:5432/otp -Dspring.rabbitmq.host=rabbitmq.antifraud-deps.svc.cluster.local -Dspring.rabbitmq.username=guest -Dspring.rabbitmq.password=guest -Dhttp.proxyHost=chckproxy.raiffeisenbank.com.ua -Dhttp.proxyPort=8080"
ENV OTP_TEMP=/tmp
ENV TZ=Europe/Kiev

>>>>>>> origin/new_releases
VOLUME /tmp

COPY cert.crt cert.crt
RUN keytool -importcert -file cert.crt -alias ca_certs -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]
