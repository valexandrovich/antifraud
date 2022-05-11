FROM openjdk:11-jre
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY docker/opentelemetry/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
ENTRYPOINT ["sh", "-c", "java -javaagent:/opentelemetry-javaagent.jar ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]