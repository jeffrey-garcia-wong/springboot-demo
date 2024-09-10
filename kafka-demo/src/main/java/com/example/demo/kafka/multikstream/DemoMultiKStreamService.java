package com.example.demo.kafka.multikstream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DemoMultiKStreamService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoMultiKStreamService.class);
    private final StreamsBuilder streamsBuilder;

    public DemoMultiKStreamService(@Autowired StreamsBuilder streamsBuilder) {
        this.streamsBuilder = streamsBuilder;
    }

    @Autowired
    public Topology startMultiKStream() {
        final KStream<Long, String> kStreamIn = streamsBuilder
                .stream("test_in",
                        Consumed.with(Serdes.Long(), Serdes.String())
                );

        kStreamIn.map((key, value) -> {
            LOGGER.info("kStreamIn key: {}, value: {}", key, value);
            return KeyValue.pair(key, value);
        })
        .split()
        .branch(
                (key, value) -> {
                    return "aaa".equalsIgnoreCase(value);
                },
                Branched.withConsumer((kStreamOut) -> kStreamOut.to("test_out_1"))
        )
        .branch(
                (key, value) -> {
                    return "bbb".equalsIgnoreCase(value);
                },
                Branched.withConsumer((kStreamOut) -> kStreamOut.to("test_out_2"))
        );

        final KStream<Long, String> kStreamOut1 = streamsBuilder
                .stream("test_out_1",
                        Consumed.with(Serdes.Long(), Serdes.String())
                )
                .peek((key,value) -> {
                    LOGGER.info("KStreamOut1 key: {}, value: {}", key, value);
                });

        final KStream<Long, String> kStreamOut2 = streamsBuilder
                .stream("test_out_2",
                        Consumed.with(Serdes.Long(), Serdes.String())
                )
                .peek((key,value) -> {
                    LOGGER.info("KStreamOut2 key: {}, value: {}", key, value);
                });

        return streamsBuilder.build();
    }


}
