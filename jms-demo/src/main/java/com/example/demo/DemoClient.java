package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DemoClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoClient.class);

    private final RetryTemplate retryTemplate;

    private final ExponentialRandomBackOffPolicy backOffPolicy;

    public DemoClient(RetryTemplate retryTemplate, ExponentialRandomBackOffPolicy backoffPolicy) {
        this.retryTemplate = retryTemplate;
        this.backOffPolicy = backoffPolicy;
    }

    public void run() {
        try {
            doWork();
        } catch (IOException e) {
            // dynamically setting the backoff policy
            backOffPolicy.setInitialInterval(1000);
            backOffPolicy.setMultiplier(2.0d);
            backOffPolicy.setMaxInterval(6000);

            retryTemplate.execute(context -> {
                // business logic here
                try {
                    return doWork();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }, retryContext -> {
                // recovery logic here
                throw new ExhaustedRetryException("retry exhausted");
            });
        }
    }

    private boolean doWork() throws IOException {
        try {
            if (("org.springframework.jms.JmsListenerEndpointContainer#0-1").equals(Thread.currentThread().getName())) {
                // simulate I/O latency
                Thread.sleep(2000);
            } else {
                throw new IOException("fake I/O exception");
            }
        } catch (InterruptedException e) {}
        return true;
    }

}
