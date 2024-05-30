# Using KStream with Protobuf

- [Docker Compose](#docker-compose)
- [Create topic for KStream](#create-topic-for-kstream)
- [Setup protobuf build environment](#setup-protobuf-build-environment)
  - [Maven](#maven)
- [Prepare protofbuf schema](#prepare-protobuf-schema)
- [Application's configuration](#applications-configuration)
- [Generate message with protobuf schema](#generate-message-with-protobuf-schema)

---
### Docker Compose
Most protobuf setup with Kafka would require schema registry. Startup a 
kafka broker with a schema registry using the command:
```shell
docker-compose down && docker-compose up
```

The content of the `docker-compose.yml` file as below.
```yaml
version: '3.3'
services:
  broker:
    image: confluentinc/cp-kafka:7.4.1
    hostname: broker
    container_name: broker
    ports:
      - 29092:29092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT,CONTROLLER:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://broker:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 1
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@broker:29093
      KAFKA_LISTENERS: PLAINTEXT://broker:9092,CONTROLLER://broker:29093,PLAINTEXT_HOST://0.0.0.0:29092
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
  schema-registry:
    image: confluentinc/cp-schema-registry:7.3.0
    hostname: schema-registry
    container_name: schema-registry
    depends_on:
      - broker
    ports:
      - 8081:8081
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: broker:9092
      SCHEMA_REGISTRY_LOG4J_ROOT_LOGLEVEL: WARN
```

---
### Create topic for KStream
Execute the command below to create the input topic that feed the kstream
```shell
docker exec -t broker kafka-topics \
--bootstrap-server broker:9092 \
--topic test_in \
--create \
--partitions 1
```

---
### Setup protobuf build environment
To build protobuf schema and use it as a POJO in java code, the build environment must 
be configured to support the build chain.

##### Maven
Add the following dependencies to `pom.xml`:
```xml
        <dependency>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java</artifactId>
            <version>3.22.2</version>
        </dependency>

        <dependency>
            <groupId>io.confluent</groupId>
            <artifactId>kafka-streams-protobuf-serde</artifactId>
            <version>7.3.0</version>
        </dependency>
```

Add the confluence repository to `pom.xml` for fetching confluent proprietary dependencies:
```xml
    <repositories>
        <repository>
            <id>confluent</id>
            <url>https://packages.confluent.io/maven/</url>
        </repository>
        <repository>
            <id>central</id>
            <url>https://repo1.maven.org/maven2/</url>
        </repository>
    </repositories>
```

Add the following build plugin to `pom.xml` for compiling protobuf schema files and generate java source.
```xml
            <plugin>
                <groupId>com.github.os72</groupId>
                <artifactId>protoc-jar-maven-plugin</artifactId>
                <version>3.11.4</version>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:3.21.1</protocArtifact>
                    <inputDirectories>
                        <include>src/main/proto</include>
                    </inputDirectories>
                </configuration>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>run</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

---
### Prepare protobuf schema
Create the schema `modelsuser.proto` inside the folder `src/main/proto`
```protobuf
syntax = "proto3";

package com.example.kafka.kstream.protobuf.demo;

option java_outer_classname = "Models";

message User {
  int32 id = 1;
  string name = 2;
  int32 age = 3;
}

```
Run maven build to compile the protobuf schema in ./src/main/proto and generate target source files
```shell
../mvnw clean compile
```

---
### Application's configuration
Startup the springboot app with the application configuration yaml `application.yml` as below:
```yaml
### Spring Kafka
---
spring:
  kafka:
    bootstrap-servers: localhost:29092
    properties:
      schema.registry.url: "http://127.0.0.1:8081"
    streams:
      application-id: streamer.local
      properties:
        retry.backoff.ms: 5000
        default.key.serde: org.apache.kafka.common.serialization.Serdes$StringSerde
        default.value.serde: io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer
        # non-recoverable error, log the error and skip to process next message
        default.deserialization.exception.handler: org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
        # when `processing.guarantee` is configured to exactly_once_v2, Kafka Streams sets the internal
        # embedded producer client with a transaction id to enable the idempotence and transactional
        # messaging features, and also sets its consumer client with the read-committed mode to only
        # fetch messages from committed transactions from the upstream producers.
        processing.guarantee: exactly_once_v2
        replication.factor: 1
        # the number of concurrency to execute stream task, bounded by the number of source topic's partition
        num.stream.threads: 1

### App specific
---
app:
  config:
    input-topic: test_in
```

---
### Generate message with protobuf schema
Execute the following command to ssh into the `schema-registry` container
```shell
docker container exec -it schema-registry /bin/bash
```

Inside the shell of `schema-registry` container, invoke `kafka-protobuf-console-producer`
> <b>Note:</b> <br/>
> The property `value.schema` must match the source protobuf schema file in `src/main/proto` 
```shell
kafka-protobuf-console-producer \
--property schema.registry.url=http://localhost:8081 \
--bootstrap-server broker:9092 \
--topic test_in \
--property parse.key=true \
--property key.separator=":" \
--property key.serializer=org.apache.kafka.common.serialization.StringSerializer \
--property value.schema='syntax = "proto3"; message User { int32 id=1; string name = 2; int32 age = 3;}'
```

Generate new message according to the schema:
```json
aaa:{ "id":1, "name":"John", "age":32 }
```