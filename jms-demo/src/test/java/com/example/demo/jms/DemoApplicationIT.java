package com.example.demo.jms;

import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;

@SpringBootTest
public class DemoApplicationIT {

    private static BrokerService broker;

    @BeforeAll
    public static void setUp() throws Exception {
        // use in-memory embedded broker service
        broker = new BrokerService();
        broker.addConnector("tcp://localhost:61616");
        broker.setUseShutdownHook(true);
        broker.setPersistent(false);
        broker.start();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        broker.stopGracefully("tcp://localhost:61616", "mailbox", 5000, 500);
    }

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private DemoClient demoClient;

    @Test
    public void verify() throws Exception {
        Mockito.doNothing().when(demoClient).run();
        JmsTemplate jmsTemplate = applicationContext.getBean(JmsTemplate.class);
        jmsTemplate.convertAndSend("mailbox", "test");
        Mockito.verify(demoClient, Mockito.times(1)).run();
    }

}
