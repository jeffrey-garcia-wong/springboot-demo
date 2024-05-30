package com.example.demo.kafka.reactive;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import reactor.kafka.sender.TransactionManager;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class DemoReactiveProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoReactiveProcessor.class);
    private final KafkaReceiver<Long, String> receiver;
    private final KafkaSender<Long, String> sender;
    private final String outputTopic;
    private final long retryMaxAttempts;
    private final long retryBackoffMs;
    private final DemoTransformer demoTransformer;

    public DemoReactiveProcessor(
            @Autowired KafkaReceiver<Long,String> kafkaReceiver,
            @Autowired KafkaSender<Long,String> kafkaSender,
            @Autowired DemoTransformer demoTransformer,
            String outputTopic,
            long retryMaxAttempts,
            long retryBackoffMs
    ) {
        this.receiver = kafkaReceiver;
        this.sender = kafkaSender;
        this.demoTransformer = demoTransformer;
        this.outputTopic = outputTopic;
        this.retryMaxAttempts = retryMaxAttempts;
        this.retryBackoffMs = retryBackoffMs;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void startProcess() {
        process().subscribe();
    }

    Flux<SenderResult<Long>> process() {
        final TransactionManager txManager = sender.transactionManager();
        final Flux<SenderResult<Long>> senderResultFlux = receiver.receiveExactlyOnce(txManager)
                .concatMap(consumerRecordFlux -> {
                    return sender.send(consumerRecordFlux.concatMap(consumerRecord -> {
                        LOGGER.debug("received message: {}", consumerRecord);
                        return transform(consumerRecord);
                    })).concatWith(txManager.commit());
                }).onErrorResume(e -> {
                    return txManager.abort().then(Mono.error(e));
                }).doOnNext(senderResult -> {
                    if (senderResult.exception() == null)
                        LOGGER.debug("send result: {}", senderResult);
                }).doOnError(e -> {
                    LOGGER.error(e.getMessage(), e);
                }).retryWhen(
                        Retry.backoff(retryMaxAttempts, Duration.ofMillis(retryBackoffMs))
                );
        return senderResultFlux;
    }

    Flux<SenderRecord<Long,String,Long>> transform(ConsumerRecord<Long,String> consumerRecord) {
        final List<String> transformed = demoTransformer.doTransform();
        final List<SenderRecord<Long,String,Long>> senderRecords = transformed.stream().map(s -> {
            return SenderRecord.create(
                    new ProducerRecord<>(
                            outputTopic,
                            consumerRecord.key(),
                            s
                    )
                    , consumerRecord.offset());
        }).collect(Collectors.toList());
        return Flux.fromIterable(senderRecords);
    }
}
