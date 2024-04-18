package com.example.demo.kafka.kstream.protobuf.demo;

import com.example.kafka.kstream.protobuf.demo.Models;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DemoStreamProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoStreamProcessor.class);
    private final Serde<String> keySerde = Serdes.String();
//    private final Serde<String> valueSerde = Serdes.String();
    private final KafkaProtobufSerde<Models.User> valueSerde = new KafkaProtobufSerde<>();
    private final StreamsBuilder streamsBuilder;
    private final String inputTopic;

    public DemoStreamProcessor(
            @Autowired
            StreamsBuilder streamsBuilder,
            @Value("${app.config.input-topic}")
            String inputTopic
    ) {
        this.streamsBuilder = streamsBuilder;
        this.inputTopic = inputTopic;
    }

    @Autowired
    public void initValueSerde(
            @Autowired
            KafkaProperties kafkaProperties
    ) {
        final String schemaRegistryUrl = kafkaProperties.buildStreamsProperties().computeIfAbsent(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                key -> { throw new RuntimeException(key + " is not configured!"); }
        ).toString();
        this.valueSerde.configure(
                Map.of(
                        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl,
                        KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, Models.User.class.getName()
                ), false
        );
    }

    @Autowired
    Topology startKStream() {
        // Construct a `KStream` from the input topic
        KStream<String, Models.User> kStreamIn = streamsBuilder
                .stream(inputTopic,
                        Consumed.with(keySerde, valueSerde)
                );

        // Applies terminal processor to kStream operation

        // debug output
        kStreamIn.peek((key,value) -> {
            LOGGER.info("kStreamIn key: {}, value: {}", key, value);
        });

        return streamsBuilder.build();
    }
}
