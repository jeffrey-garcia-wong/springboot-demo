package com.example.demo.kafka.kstream;

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

/**
 * <h1>Kafka Stream Configuration</h1>
 *
 * This class registers the following:
 * <ul>
 *     <li>the stream config {@link KafkaStreamsConfiguration}</li>
 *     <li>the stream builder factory bean {@link StreamsBuilderFactoryBean}</li>
 * </ul>
 *
 * @see org.springframework.kafka.annotation.EnableKafkaStreams
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KafkaProperties.class)
public class DemoStreamConfig {
    /**
     * Create the default {@link KafkaStreamsConfiguration}.<p/>
     *
     * @implNote
     * This method reference KafkaStreamsAnnotationDrivenConfiguration#defaultKafkaStreamsConfig(Environment),
     * providing a way to intercept  built-in kafka properties, thus providing granular
     * control for developers to override the enablement/behavior of {@link StreamsBuilderFactoryBean}.<p/>
     *
     * @param kafkaProperties the {@link KafkaProperties} object with kafka's configuration
     * @return an instance of {@link KafkaStreamsConfiguration}
     */
    @Bean(name = DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig(
            @Autowired KafkaProperties kafkaProperties
    ) {
        final Map<String, Object> streamsProperties = kafkaProperties.buildStreamsProperties();
        return new KafkaStreamsConfiguration(streamsProperties);
    }

    /**
     * Create the default {@link StreamsBuilderFactoryBean}.<p/>
     *
     * This bean is activated when:
     * <ul>
     *     <li>the stream config `defaultKafkaStreamsConfig` is present, and</li>
     *     <li>the factory bean `defaultKafkaStreamsBuilder` does not exist</li>
     * </ul>
     *
     * @implNote
     * This method reference {@link KafkaStreamsDefaultConfiguration#defaultKafkaStreamsBuilder(ObjectProvider, ObjectProvider)}.
     *
     * @see
     * {@link KafkaStreamsDefaultConfiguration}
     *
     * @param streamsConfigProvider the streams config {@link KafkaStreamsConfiguration}.
     * @param configurerProvider the configurer.
     * @return an instance of {@link StreamsBuilderFactoryBean}.
     */
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
