package com.example.demo.kafka.client;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Map;

/**
 * <h1>Kafka Consumer Configuration</h1>
 *
 * @see org.apache.kafka.clients.consumer.ConsumerConfig
 * @see ConsumerFactory
 * @see KafkaProperties
 * @see org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
 *
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KafkaProperties.class)
public class DemoConsumerConfig {
    /**
     * Create the default consumer factory.<p/>
     *
     * @param kafkaProperties the {@link KafkaProperties} ingested to customize consumer's behavior
     * @param customizers the {@link ObjectProvider} used to customize the consumer factory
     * @return an instance of {@link ConsumerFactory}
     *
     * @implNote
     * This method reference {@link org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration},
     * providing a way to intercept  built-in kafka properties<p/>
     *
     * @see
     * <a href="https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html">Consumer Configs</a>
     *
     */
    @Bean
    public DefaultKafkaConsumerFactory<Long, String> kafkaConsumerFactory(
            @Autowired KafkaProperties kafkaProperties,
            @Autowired ObjectProvider<DefaultKafkaConsumerFactoryCustomizer> customizers)
    {
        final Map<String, Object> config = kafkaProperties.buildConsumerProperties();
        // disable consumer auto-commit
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        final DefaultKafkaConsumerFactory<Long, String> factory =
                new DefaultKafkaConsumerFactory<>(config);
        customizers.orderedStream().forEach((customizer) ->
                customizer.customize(factory));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, String> kafkaListenerContainerFactory(
            @Autowired KafkaProperties kafkaProperties,
            @Autowired ConsumerFactory<Long, String> kafkaConsumerFactory
    ) {
        final ConcurrentKafkaListenerContainerFactory<Long, String> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(kafkaConsumerFactory);
        // single listener per consumer instance to guarantee consumption ordering
        listenerContainerFactory.setConcurrency(1);
        // manual acknowledgement
        listenerContainerFactory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        listenerContainerFactory.getContainerProperties()
                .setAsyncAcks(false);
        // TODO: MAX_POLL_INTERVAL_MS
        return listenerContainerFactory;
    }
}
