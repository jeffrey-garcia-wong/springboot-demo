#!/usr/bin/env bash

docker-compose -f docker-compose-kafka.yml down --remove-orphans
docker-compose -f docker-compose-kafka.yml up --build