#!/usr/bin/env bash

docker-compose -f docker-compose-oracle.yml down --remove-orphans
docker-compose -f docker-compose-oracle.yml up --build