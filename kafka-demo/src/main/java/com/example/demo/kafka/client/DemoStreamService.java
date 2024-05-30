package com.example.demo.kafka.client;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class DemoStreamService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoStreamService.class);
    private final StreamsBuilder streamsBuilder;
    private final StreamsBuilderFactoryBean streamBuilderFactoryBean;
    private final String outputTopic;
    private final String stateStore;

    public DemoStreamService(
            @Autowired
            StreamsBuilder streamsBuilder,
            @Autowired
            StreamsBuilderFactoryBean streamBuilderFactoryBean,
            @Value("${app.config.output-topic}")
            String outputTopic,
            @Value("${app.config.state-store}")
            String stateStore
    ) {
        this.streamsBuilder = streamsBuilder;
        this.streamBuilderFactoryBean = streamBuilderFactoryBean;
        this.outputTopic = outputTopic;
        this.stateStore = stateStore;
    }

    @Autowired
    public Topology createKTable() {
        final KStream<Long, String> kStream = streamsBuilder
                .stream(outputTopic,
                        Consumed.with(
                                Serdes.Long(),
                                Serdes.String()
                        )
                );
        final KTable<Long, String> kTable =
                kStream.toTable(Materialized.as(stateStore));
        return streamsBuilder.build();
    }

    private Optional<String> searchKTableByKey(Long key) {
        if (this.streamBuilderFactoryBean != null) {
            final KafkaStreams kafkaStreams = this.streamBuilderFactoryBean.getKafkaStreams();
            Objects.requireNonNull(kafkaStreams);
            final ReadOnlyKeyValueStore<Long, String> keyValueStore = kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(
                            stateStore,
                            QueryableStoreTypes.keyValueStore()
                    )
            );
            String value = keyValueStore.get(key);
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public boolean hasValueMutated(Long incomingKey, String incomingValue) {
        final Optional<String> optionalValue = searchKTableByKey(incomingKey);
        if (optionalValue.isPresent()) {
            final String cachedValue = optionalValue.get();
            if (cachedValue.equals(incomingValue)) {
                return false;
            }
        }
        return true;
    }
}
