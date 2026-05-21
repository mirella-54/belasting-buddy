#!/bin/bash
set -e

docker-compose down -v
mvn clean package -pl user-management-service -DskipTests
docker-compose up -d --build
