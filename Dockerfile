FROM nexus.otpbank.com.ua:19443/repository/openjdk:11-jre

ENV JAVA_OPTS="-Xms256m -Xmx2G -XX:+ExitOnOutOfMemoryError -Dhttp.proxyHost=chckproxy -Dhttp.proxyPort=8080 -Dhttps.proxyHost=chckproxy -Dhttps.proxyPort=8080 -Dhttp.nonProxyHosts="localhost|127.0.*|*.raiffeisenbank.com.ua|10.247.*|10.244.*|195.248.*|10.233.*|antifrauddbdev.raiffeisenbank.com.ua|*.svc.cluster.*|wrapper.antifraud.svc.cluster.local" -Djava.security.egd=file:///dev/./urandom -Dsecurerandom.source=file:///dev/./urandom"
ENV TZ=Europe/Kiev

COPY cert.crt cert.crt
RUN keytool -importcert -file cert.crt -alias ca_certs -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt
COPY data.crt data.crt
RUN keytool -importcert -file data.crt -alias data_cert -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY docker/opentelemetry/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
#ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar --spring.config.name=application --spring.config.location=$PATH_TO_PROPERTIES ${0} ${@}"]