package com.example.demo.kafka.client;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * <h1>Kafka Producer Configuration</h1>
 *
 * @see org.apache.kafka.clients.producer.ProducerConfig
 * @see ProducerFactory
 * @see KafkaProperties
 * @see org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
 *
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KafkaProperties.class)
public class DemoProducerConfig {

    /**
     * Create the default producer factory.<p/>
     *
     * @param kafkaProperties the {@link KafkaProperties} ingested to customize producer's behavior
     * @param customizers the {@link ObjectProvider} used to customize the producer factory
     * @return an instance of {@link ProducerFactory}
     *
     * @implNote
     * This method reference {@link org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration#kafkaProducerFactory(ObjectProvider)},
     * providing a way to intercept  built-in kafka properties.
     *
     * @see
     * <a href="https://docs.confluent.io/platform/current/installation/configuration/producer-configs.html">Producer Configs</a>
     *
     */
    @Bean
    public ProducerFactory<?, ?> kafkaProducerFactory(
            @Autowired KafkaProperties kafkaProperties,
            @Autowired ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers
    ) {
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties());
        String transactionIdPrefix = kafkaProperties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    /**
     * Create an instance of Kafka Template for message sending using
     * the customised producer's factory bean.<p/>
     *
     * @param producerFactory an instance of {@link ProducerFactory}
     * @return an instance of {@link KafkaTemplate}
     */
    @Bean
    public KafkaTemplate<?,?> kafkaTemplate(
            @Autowired ProducerFactory<?,?> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
