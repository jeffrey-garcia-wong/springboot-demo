package com.example.demo.kafka.multikstream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;

import java.util.Map;

import static org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_BUILDER_BEAN_NAME;
import static org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KafkaProperties.class)
public class DemoMultiKStreamConfig {

    @Bean(name = DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig(
            @Autowired KafkaProperties kafkaProperties
    ) {
        final Map<String, Object> streamsProperties = kafkaProperties.buildStreamsProperties();
        return new KafkaStreamsConfiguration(streamsProperties);
    }

    @ConditionalOnBean(name = DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    @ConditionalOnMissingBean(name = DEFAULT_STREAMS_BUILDER_BEAN_NAME)
    @Bean(name = DEFAULT_STREAMS_BUILDER_BEAN_NAME)
    public StreamsBuilderFactoryBean streamBuilderFactoryBean(
            @Qualifier(DEFAULT_STREAMS_CONFIG_BEAN_NAME)
            ObjectProvider<KafkaStreamsConfiguration> streamsConfigProvider,
            ObjectProvider<StreamsBuilderFactoryBeanConfigurer> configurerProvider)
    {
        KafkaStreamsConfiguration streamsConfig = streamsConfigProvider.getIfAvailable();
        if (streamsConfig != null) {
            final StreamsBuilderFactoryBean fb = new StreamsBuilderFactoryBean(streamsConfig);
            configurerProvider.orderedStream().forEach(configurer -> configurer.configure(fb));
            return fb;
        }
        else {
            throw new UnsatisfiedDependencyException(KafkaStreamsDefaultConfiguration.class.getName(),
                    DEFAULT_STREAMS_BUILDER_BEAN_NAME, "streamsConfig", "There is no '" +
                    DEFAULT_STREAMS_CONFIG_BEAN_NAME + "' " + KafkaStreamsConfiguration.class.getName() +
                    " bean in the application context.\n" +
                    "Consider declaring one or don't use @EnableKafkaStreams.");
        }
    }
}
