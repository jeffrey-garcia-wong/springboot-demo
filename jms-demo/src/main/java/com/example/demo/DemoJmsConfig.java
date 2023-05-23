package com.example.demo;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration(proxyBeanMethods = false)
@EnableJms
public class DemoJmsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoJmsConfig.class);

    @Value("${activemq.broker.url}")
    private String brokerUrl;
    @Value("${activemq.broker.username}")
    private String username;
    @Value("${activemq.broker.password}")
    private String password;

    @Bean
    public RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(-1);
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setInitialRedeliveryDelay(10000L);
        redeliveryPolicy.setUseCollisionAvoidance(true);
        redeliveryPolicy.setBackOffMultiplier(1.5D);
        redeliveryPolicy.setMaximumRedeliveryDelay(30000L);
        redeliveryPolicy.setDestination(new ActiveMQQueue("mailbox"));
        return redeliveryPolicy;
    }

    @Bean
    public ConnectionFactory connectionFactory(RedeliveryPolicy redeliveryPolicy) {
//        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
//        return connectionFactory;

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(username);
        connectionFactory.setPassword(password);

//        RedeliveryPolicyMap redeliveryPolicyMap = connectionFactory.getRedeliveryPolicyMap();
//        redeliveryPolicyMap.put(new ActiveMQQueue("mailbox"), redeliveryPolicy());
//        connectionFactory.setRedeliveryPolicyMap(redeliveryPolicyMap);
//        RedeliveryPolicy redeliveryPolicy = connectionFactory.getRedeliveryPolicy();

        connectionFactory.setRedeliveryPolicy(redeliveryPolicy);

        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory){
        JmsTemplate template = new JmsTemplate(connectionFactory);
        return template;
    }

//    @Bean
//    public PlatformTransactionManager transactionManager() {
//        JmsTransactionManager transactionManager = new JmsTransactionManager();
//        transactionManager.setConnectionFactory(connectionFactory());
//        return transactionManager;
//    }

    @Bean
    public JmsListenerContainerFactory<?> myFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // You could still override some of Boot's default if necessary.
        factory.setConcurrency("1-2"); // auto-scaling consumers
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        factory.setSessionTransacted(true);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

}
