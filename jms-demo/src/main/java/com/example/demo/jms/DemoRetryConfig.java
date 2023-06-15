package com.example.demo.jms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration(proxyBeanMethods = false)
@EnableRetry
public class DemoRetryConfig {
    @Value("${retry.backoff.initialInterval}")
    private long initialInterval;
    @Value("${retry.backoff.multiplier}")
    private double multiplier;
    @Value("${retry.backoff.maxInterval}")
    private long maxInterval;

    @Bean
    public ExponentialRandomBackOffPolicy backOffPolicy() {
        final ExponentialRandomBackOffPolicy backOffPolicy = new ExponentialRandomBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);
        return backOffPolicy;
    }

    @Bean
    public RetryTemplate retryTemplate(ExponentialRandomBackOffPolicy backOffPolicy) {
        final RetryTemplate retryTemplate = RetryTemplate
                .builder()
                .maxAttempts(3)
//                .exponentialBackoff(1000, 1.5d, 5000, true)
                .retryOn(RuntimeException.class)
                .build();


//        backOffPolicy.setMaxInterval(1000L);
//        backOffPolicy.setMultiplier(2.0D);
//        backOffPolicy.setMaxInterval(10000L);

        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

}
