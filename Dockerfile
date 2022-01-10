FROM openjdk:11-jre
ENV JAVA_OPTS="-Dspring.datasource.url=jdbc:postgresql://antifrauddbdev:5432/otp -Dspring.rabbitmq.host=rabbitmq -Dspring.rabbitmq.username=guest -Dspring.rabbitmq.password=guest"
ENV OTP_TEMP=/tmp
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]