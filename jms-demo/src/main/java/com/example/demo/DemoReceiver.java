package com.example.demo;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class DemoReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoReceiver.class);

    private DemoClient demoClient;

    public DemoReceiver(DemoClient demoClient) {
        this.demoClient = demoClient;
    }

    @JmsListener(destination = "mailbox", containerFactory = "myFactory")
    public void receiveMessage(ActiveMQTextMessage msg) {
        try {
            LOGGER.debug("Received <" + msg.getMessageId() + "> - " + msg.getText());
            demoClient.run();
        } catch (Exception e) {
             e.printStackTrace();
        } finally {
            LOGGER.debug("process finished");
        }
    }

}
