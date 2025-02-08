package com.example.demo.camel;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.ShutdownStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class CamelDemoAppIT {

//    @Autowired
//    private CamelContext camelContext;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
//        ShutdownStrategy shutdownStrategy = camelContext.getShutdownStrategy();
//        shutdownStrategy.setTimeout(20);
//        SpringApplication.exit(applicationContext);
    }
}
