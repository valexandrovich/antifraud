## Setup Docker Compose

```
mvn clean install
docker-compose -f docker-compose.yml up
```
The application starts with database, queue, logger, prometheus, and grafana

## Manually setup system

### Setup DB

Create user and database named `otp` with password `otp` 

### Build application

`mvn clean install`

### Create database

```
cd db
mvn liquibase:update
cd ..
```

### Run services (separate TTYs required)
```
java -Dspring.datasource.url=jdbc:postgresql://10.0.1.14:5432/otp -Dspring.rabbitmq.host=63.34.168.20 -Dspring.rabbitmq.username=dr -Dspring.rabbitmq.password=dr -jar downloader/target/downloader-0.0.1-SNAPSHOT.jar
java -Dspring.datasource.url=jdbc:postgresql://10.0.1.14:5432/otp -Dspring.rabbitmq.host=63.34.168.20 -Dspring.rabbitmq.username=dr -Dspring.rabbitmq.password=dr -jar importer/target/import-0.0.1-SNAPSHOT.jar
java -Dspring.datasource.url=jdbc:postgresql://10.0.1.14:5432/otp -Dspring.rabbitmq.host=63.34.168.20 -Dspring.rabbitmq.username=dr -Dspring.rabbitmq.password=dr -jar scheduler/target/scheduler-0.0.1-SNAPSHOT.jar
java -Dspring.datasource.url=jdbc:postgresql://10.0.1.14:5432/otp -Dspring.rabbitmq.host=63.34.168.20 -Dspring.rabbitmq.username=dr -Dspring.rabbitmq.password=dr -jar scheduler_test/target/scheduler_test-0.0.1-SNAPSHOT.jar
java -Dspring.datasource.url=jdbc:postgresql://10.0.1.14:5432/otp -Dspring.rabbitmq.host=63.34.168.20 -Dspring.rabbitmq.username=dr -Dspring.rabbitmq.password=dr -jar web/target/web-0.0.1-SNAPSHOT.jar
```
---

## Build docker image

Install: `./mvnw install`

Build: `docker build --build-arg JAR_FILE=common/target/*.jar --build-arg DB_URL=jdbc:postgresql://10.0.1.14:5432/otp --build-arg MQ_HOST=63.34.168.20 --build-arg MQ_USER=dr --build-arg MQ_PASS=dr -t solidity/otp .`

Run: `docker run -p 8080:8080 solidity/otp`

Note: Requires separate Postgres available for solidity/otp container. For this on Windows update `pg_hba.conf` to include local IP/network:

`host    all             all             10.0.1.14/32            md5`

## Docker Compose

```
docker-compose up -d
```

### Rebuild and restart single container (e.g. web)

```
docker-compose up -d --no-deps --build web
```

### Loki Docker Driver Client 

```
docker plugin install grafana/loki-docker-driver:latest --alias loki --grant-all-permissions
```

## ElasticSearch

Two configurations provided - single node (`elastic`) and cluster (`elastic01`, `elastic02`, `elastic03`).

The unnecessary configuration could be commented out in Docker-Compose YAML.

For Windows to run image(s) successfully, enter:
```
wsl -d docker-desktop
sysctl -w vm.max_map_count=262144
```