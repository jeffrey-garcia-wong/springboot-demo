package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { DemoRetryConfig.class })
@TestPropertySource(properties = {
        "retry.backoff.initialInterval = 500",
        "retry.backoff.multiplier = 2.0",
        "retry.backoff.maxInterval = 1000"
})
public class DemoClientTests {

    @Autowired RetryTemplate retryTemplate;
    @Autowired ExponentialRandomBackOffPolicy backOffPolicy;

    @Test
    public void run() {
        try {
            doSomething();
        } catch (RuntimeException e) {
            // dynamically adjusting the backoff policy
//            backOffPolicy.setInitialInterval(1000);
//            backOffPolicy.setMultiplier(2.0d);
//            backOffPolicy.setMaxInterval(6000);

            retryTemplate.setBackOffPolicy(backOffPolicy);
            assertThrows(RuntimeException.class, () -> {
                retryTemplate.execute(retryContext -> doSomething());
            });
        }
    }

    private boolean doSomething() {
        throw new RuntimeException();
    }

}
