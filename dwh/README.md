Build
===

```
mvn clean package
```

Run
===

```
java -Dspring.rabbitmq.host=63.34.168.20 -Dspring.rabbitmq.username=dr -Dspring.rabbitmq.password=dr -jar dwh/target/*.jar
```
* local DWH mock and Postgres are required. See docker-compose in the project root to find properties to setup remote databases 

Trigger
===
Send message to `otp-etl.dwh` queue:

```json
{"lastModified":1607464800000}
```
The service will then import all records with ArcDate after 2020-12-09