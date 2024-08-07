### Produce message into topic
```shell
docker container exec -it kafka /bin/bash

kafka-console-producer \
--broker-list kafka:9092 \
--topic test-in \
--property parse.key=true \
--property key.separator=":" \
--property key.serializer=org.apache.kafka.common.serialization.StringSerializer \
--property value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

sample input data:
```shell
a001:testing
a002:debug
a003:testing
```

```shell
docker container exec -it kafka /bin/bash

kafka-console-producer \
--broker-list kafka:9092 \
--topic user \
--property parse.key=true \
--property key.separator=":" \
--property key.serializer=org.apache.kafka.common.serialization.StringSerializer \
--property value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

```shell
a001:John
a002:Peter
a003:Sam
```

### Consume message from topic
```shell
docker exec -t kafka kafka-console-consumer \
--topic test-out \
--bootstrap-server kafka:9092 \
--from-beginning \
--property print.offset=true \
--property print.key=true \
--property key.separator=":" \
--property print.partition=true
```

---
docker exec -t kafka kafka-console-consumer \
--topic debezium-demo-app-KSTREAM-JOINOTHER-0000000007-store-changelog \
--bootstrap-server kafka:9092 \
--from-beginning \
--property print.offset=true \
--property print.key=true \
--property key.separator=":" \
--property print.partition=true