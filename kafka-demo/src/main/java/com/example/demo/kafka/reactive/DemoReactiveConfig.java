package com.example.demo.kafka.reactive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class DemoReactiveConfig {

    @Value(value = "${app.config.input-topic}")
    private String inputTopic;

    @Value(value = "${app.config.output-topic}")
    private String appOutputTopic;

    @Value(value = "${spring.kafka.consumer.properties.retry.backoff.ms}")
    private long consumerRetryBackoffMs;

    @Bean
    public ReceiverOptions<Long, String> kafkaReceiverOptions(
            @Autowired
            KafkaProperties kafkaProperties
    ) {
        final Map<String, Object> config = kafkaProperties.buildConsumerProperties();
        ReceiverOptions<Long, String> receiverOptions = ReceiverOptions.create(config);
        return receiverOptions
                .subscription(Collections.singletonList(inputTopic));
    }

    @Bean
    public SenderOptions<Long, String> kafkaSenderOptions(
            @Autowired
            KafkaProperties kafkaProperties
    ) {
        final Map<String, Object> config = kafkaProperties.buildProducerProperties();
        return SenderOptions.create(config);
    }

    @Bean
    public KafkaReceiver<Long, String> kafkaReceiver(
            @Autowired
            ReceiverOptions<Long, String> receiverOptions
    ) {
        return KafkaReceiver.create(receiverOptions);
    }

    @Bean
    public KafkaSender<Long, String> kafkaSender(
            @Autowired
            SenderOptions<Long, String> senderOptions
    ) {
        return KafkaSender.create(senderOptions);
    }

    @Bean
    public DemoTransformer transformer() {
        return new DemoTransformer();
    }
    @Bean
    public DemoReactiveProcessor reactiveConsumer(
            @Autowired KafkaReceiver<Long, String> kafkaReceiver,
            @Autowired KafkaSender<Long, String> kafkaSender,
            @Autowired DemoTransformer demoTransformer
    ) {
        return new DemoReactiveProcessor(
                kafkaReceiver,
                kafkaSender,
                demoTransformer,
                appOutputTopic,
                Long.MAX_VALUE, // infinite retry
                consumerRetryBackoffMs
        );
    }
}
