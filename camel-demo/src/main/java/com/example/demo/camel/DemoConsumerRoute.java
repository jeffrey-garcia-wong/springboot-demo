package com.example.demo.camel;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DemoConsumerRoute extends RouteBuilder {
    private final DemoProcessor processor;

    public DemoConsumerRoute(DemoProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void configure() throws Exception {
        from("activemq:my-activemq-queue")
                .transacted()
                .log(LoggingLevel.INFO, "received message")
                .process(this.processor)
                .to("log:finish-message-consumption")
                .end();
    }
}
