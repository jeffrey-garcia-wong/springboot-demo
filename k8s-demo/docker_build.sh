#!/bin/bash

./gradlew clean build
docker build --build-arg JAR_FILE=build/libs/demo-0.0.1-SNAPSHOT.jar -t jeffreygarcia/k8s-demo .