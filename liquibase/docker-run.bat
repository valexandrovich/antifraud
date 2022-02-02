@ECHO OFF

docker run --rm -v %cd%\src\main\resources\changelog:/liquibase/changelog liquibase/liquibase --url=jdbc:postgresql://10.0.1.14:5432/otp --username=otp --password=otp --changeLogFile=changelog/db.changelog-master.yaml --logLevel=info update
