#!/usr/bin/env bash

./mvnw clean install
docker build --build-arg JAR_FILE=downloader/target/*.jar -t otp/downloader .
docker build --build-arg JAR_FILE=importer/target/*.jar -t otp/importer .
docker build --build-arg JAR_FILE=scheduler/target/*.jar -t otp/scheduler .
docker build --build-arg JAR_FILE=scheduler_test/target/*.jar -t otp/scheduler_test .
docker build --build-arg JAR_FILE=web/target/*.jar -t otp/web .

cd db || exit
./mvnw liquibase:update
cd ..
