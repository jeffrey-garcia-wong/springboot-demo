package com.example.demo.kafka.kstream.protobuf.demo;

import com.example.kafka.kstream.protobuf.demo.Models;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DemoProcessorSupplier implements ProcessorSupplier<String, Models.User, String, Models.User> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoProcessorSupplier.class);

    @Override
    public Processor<String, Models.User, String, Models.User> get() {
        return new Processor<>() {
            @Override
            public void process(Record<String, Models.User> record) {
                final Models.User user = record.value();
                LOGGER.info("user: {}", user);
            }
        };
    }
}
