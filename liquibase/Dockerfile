FROM liquibase/liquibase:latest
ADD liquibase/src/main/resources/changelog /liquibase/changelog

CMD ["sh", "-c", "docker-entrypoint.sh --url=${URL} --username=${USERNAME} --password=${PASSWORD} --changelogFile=db.changelog-master.yaml --logLevel=debug update"]
