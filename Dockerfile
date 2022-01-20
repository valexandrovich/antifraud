FROM openjdk:11-jre

ENV JAVA_OPTS="-Dhttp.proxyHost=chckproxy.raiffeisenbank.com.ua -Dhttp.proxyPort=8080 --spring.config.name=application --spring.config.location=$PATH_TO_PROPERTIES"
ENV OTP_TEMP=/tmp
ENV TZ=Europe/Kiev

VOLUME /tmp

COPY cert.crt cert.crt
RUN keytool -importcert -file cert.crt -alias ca_certs -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]
