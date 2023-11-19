package com.example.demo.kafka.kstream;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DemoProcessorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoProcessorService.class);
    private final Serde<Long> keySerde = Serdes.Long();
    private final Serde<String> valueSerde = Serdes.String();
    private final StreamsBuilder streamsBuilder;
    private final String inputTopic;
    private final String outputTopic;
    private final String stateStore;

    public DemoProcessorService(
            @Autowired
            StreamsBuilder streamsBuilder,
            @Value("${app.config.input-topic}")
            String inputTopic,
            @Value("${app.config.output-topic}")
            String outputTopic,
            @Value("${app.config.state-store}")
            String stateStore
    ) {
        this.streamsBuilder = streamsBuilder;
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.stateStore = stateStore;
    }

    @Autowired
    Topology startKStream() {
        // Construct a `KStream` from the input topic
        KStream<Long, String> kStreamIn = streamsBuilder
                .stream(inputTopic,
                        Consumed.with(keySerde, valueSerde)
                );

        // Construct a `KStream` from the output topic
        KStream<Long, String> kStreamOut = streamsBuilder
                .stream(outputTopic,
                        Consumed.with(keySerde, valueSerde)
                );

        // Construct a `Ktable` from the output topic
        KTable<Long, String> kTable = kStreamOut.toTable(
                Materialized.as(stateStore));

        // `LeftJoin` input KStream with KTable
        // so keys that exist only in input KStream and keys exist in both will be selected
        KStream<Long, String> kStreamJoin = kStreamIn.leftJoin(kTable, (value1, value2) -> {
            // TODO:
            // If incoming of duplicate messages arrives faster than the write to KTables,
            // the first check will always evaluate to false and breaks the de-duplication.
            if (value1.equals(value2)) return null;
            return value1;
        }).filter((key, value) -> value != null);

        // send the result to output KStream
        kStreamJoin.to(outputTopic,
                Produced.with(keySerde,valueSerde));

        // debug output
        kStreamIn.peek((key,value) -> {
            LOGGER.info("kStreamIn key: {}, value: {}", key, value);
        });
        kStreamJoin.peek((key,value) -> {
            LOGGER.info("kStreamJoin key: {}, value: {}", key, value);
        });
        kStreamOut.peek((key,value) -> {
            LOGGER.info("kStreamOut key: {}, value: {}", key, value);
        });

        return streamsBuilder.build();
    }
}
