#!/usr/bin/env bash

curl -i -X DELETE localhost:8083/connectors/customers-connector

curl -i -X POST -H "Accept:application/json" \
  -H "Content-Type:application/json" \
  localhost:8083/connectors \
  -d @connector-config.json