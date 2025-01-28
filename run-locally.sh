#!/bin/sh

set -e

mvn clean package

echo
echo "*** Available queues: ***"
aws --endpoint-url http://localhost:4566 sqs list-queues
echo

echo
echo "*** Starting word-count-app ***"
echo

# How+to+debug+a+Dockerized+Service
JAVA_OPTIONS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n -Xms256m -Xmx1024m"

java \
  ${JAVA_OPTIONS} \
  -jar target/beam-app-1.0-SNAPSHOT.jar \
  --dbUrl=jdbc:postgresql://localhost:5432/beam \
  --dbUser=postgres \
  --dbPassword=changeme \
  --inputProject=ingestion-updater-input \
  --outputProject=ingestion-updater-output \
  --numWorkers=2 \
  --autoscalingAlgorithm=THROUGHPUT_BASED \
  --maxNumWorkers=4


## Docker Run
java \
  ${JAVA_OPTIONS} \
  -jar target/beam-app-1.0-SNAPSHOT.jar \
--dbUrl=jdbc:postgresql://localhost:5432/postgres
--dbUser=postgres
--dbPassword=changeme
--inputProject=ingestion-updater-input
--outputProject=ingestion-updater-output
--queueCsv=http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/dev-queue-csv
--awsRegion=us-east-1