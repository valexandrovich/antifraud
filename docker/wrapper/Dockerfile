FROM python:3.10-alpine
ARG SETTINGS=""
ENV ARG_SETTINGS=$SETTINGS
WORKDIR /usr/src/wrapper
VOLUME /usr/src/wrapper/files
ADD config.ini .
ADD wrapper.py .
EXPOSE 8000:8000
ENTRYPOINT ["python3",  "wrapper.py", "server.domain=wrapper", "${ARG_SETTINGS}"]