FROM openjdk:11-jre
ENV JAVA_OPTS="-Dspring.datasource.url=jdbc:postgresql://antifrauddbdev.raiffeisenbank.com.ua:5432/otp -Dspring.rabbitmq.host=rabbitmq.antifraud-deps.svc.cluster.local -Dspring.rabbitmq.username=guest -Dspring.rabbitmq.password=guest"
ENV OTP_TEMP=/tmp
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]