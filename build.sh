#!/usr/bin/env bash

./mvnw clean install

cd liquibase || exit
../mvnw liquibase:update
cd ..
