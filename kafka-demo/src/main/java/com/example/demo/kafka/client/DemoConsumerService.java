package com.example.demo.kafka.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DemoConsumerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoConsumerService.class);
    private final DemoProducerService producerService;
    private final DemoStreamService streamerService;

    public DemoConsumerService(
            @Autowired
            DemoProducerService producerService,
            @Autowired
            DemoStreamService streamerService
    ) {
        this.producerService = producerService;
        this.streamerService = streamerService;
    }

    @KafkaListener(topics = "${app.config.input-topic}")
    @SuppressWarnings("unused")
    public void consumeMessage(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) Long key,
            @Payload String value,
            Acknowledgment acknowledgment
    ) {
        try {
            LOGGER.debug(String.format("Consume topic %s from partition %d with offset %d: key = %s, value = %s", topic, partition, offset, key, value));
            process(key, value, acknowledgment);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            // TODO: do we discard message or infinite retry?
            // record will be redelivered after the sleep duration
            acknowledgment.nack(Duration.ofMillis(5000));
        }
    }

    void process(Long key, String value, Acknowledgment acknowledgment) throws Exception {
        if (streamerService!=null && !streamerService.hasValueMutated(key, value)) {
            acknowledgment.acknowledge();
            return;
        }
        // Block the async send and wait for result before acknowledging (committing)
        // current offset. This ensures the consumer is only fetching new message
        // when current message is successfully persisted in the output topic
        this.producerService
                .sendMessageAsync(key, value)
                .get();
        acknowledgment.acknowledge();
    }
}
