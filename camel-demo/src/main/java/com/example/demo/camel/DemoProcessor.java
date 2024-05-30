package com.example.demo.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DemoProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        LOGGER.info("start processing message");
        String message = exchange.getIn().getBody(String.class);
        LOGGER.info("message content: {}", message);
    }
}
