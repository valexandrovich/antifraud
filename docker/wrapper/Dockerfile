FROM python:3.10-alpine
ARG SETTINGS=""
ENV ARG_SETTINGS=$SETTINGS
WORKDIR /usr/src/wrapper
VOLUME /usr/src/wrapper/files
COPY docker-config.ini ./config.ini
COPY wrapper.py .
EXPOSE 9559:9559
ENTRYPOINT ["python3",  "wrapper.py", "server.domain=wrapper", "${ARG_SETTINGS}"]