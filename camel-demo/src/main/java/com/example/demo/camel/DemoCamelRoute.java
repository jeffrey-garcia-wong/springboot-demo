package com.example.demo.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DemoCamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:test")
                .process(exchange -> {
                    String message = (String) exchange.getIn().getBody();
                    exchange.getIn().setBody(String.format("%s %s", message, System.nanoTime()));
                })
                .log("${body}")
                //send this message to ActiveMQ queue
                .to("activemq:my-activemq-queue");
    }
}
