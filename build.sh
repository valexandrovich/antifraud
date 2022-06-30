#!/usr/bin/env bash

./mvnw clean install || exit

cd liquibase || exit
../mvnw liquibase:update
cd ..
