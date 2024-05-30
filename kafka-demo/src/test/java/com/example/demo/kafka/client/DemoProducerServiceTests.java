package com.example.demo.kafka.client;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DemoProducerServiceTests {

    @Test
    public void verifySendMessageAsync() {
        final Long key = 12345678L;
        final String value = "Hello World!";

        final Serializer<Long> keySerializer = mock(LongSerializer.class);
        final Serializer<String> valueSerializer = mock(StringSerializer.class);

        final MockProducer<Long, String> mockProducer = new MockProducer<>(
                Cluster.empty(), false, null,
                keySerializer, valueSerializer
        );

        final ProducerFactory<Long, String> mockProducerFactory = mock(ProducerFactory.class);
        when(mockProducerFactory.createProducer()).thenReturn(mockProducer);

        final KafkaTemplate<Long, String> kafkaTemplate = new KafkaTemplate<>(mockProducerFactory);

        final DemoProducerService producerService =
                new DemoProducerService(kafkaTemplate, "output-topic");

        try {
            producerService.sendMessageAsync(key, value);
            final Map<Long, String> result = mockProducer.history().stream()
                    .collect(Collectors.toMap(ProducerRecord::key,ProducerRecord::value));
            assertEquals(1, result.size());
            assertEquals(value, result.get(key));
        } catch (Exception e) {
            fail("Exception should not be thrown", e);
        }
    }

    @Test
    public void verifySendMessageAsync_exceptionBeforeSend() {
        final Long key = 12345678L;
        final String value = "Hello World!";

        final Serializer<Long> keySerializer = mock(LongSerializer.class);
        when(keySerializer.serialize(anyString(),anyLong())).thenThrow(new SerializationException("dummy-error"));
        final Serializer<String> valueSerializer = mock(StringSerializer.class);

        final MockProducer<Long, String> mockProducer = new MockProducer<>(
                Cluster.empty(), false, null,
                keySerializer, valueSerializer
        );

        final ProducerFactory<Long, String> mockProducerFactory = mock(ProducerFactory.class);
        when(mockProducerFactory.createProducer()).thenReturn(mockProducer);

        final KafkaTemplate<Long, String> kafkaTemplate = new KafkaTemplate<>(mockProducerFactory);

        final DemoProducerService producerService =
                new DemoProducerService(kafkaTemplate, "output-topic");

        try {
            producerService.sendMessageAsync(key, value);
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertTrue(e instanceof SerializationException);
        }
    }
}
