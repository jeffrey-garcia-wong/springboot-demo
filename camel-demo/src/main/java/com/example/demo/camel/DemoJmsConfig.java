package com.example.demo.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.camel.CamelContext;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

@Configuration(proxyBeanMethods = false)
@EnableJms
public class DemoJmsConfig {

    @Value("${spring.activemq.broker.url}")
    private String brokerUrl;
    @Value("${spring.activemq.broker.username}")
    private String username;
    @Value("${spring.activemq.broker.password}")
    private String password;

    @Bean("jmsConnectionFactory")
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean("jmsTransactionManager")
    public PlatformTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        JmsTransactionManager transactionManager = new JmsTransactionManager();
        transactionManager.setConnectionFactory(connectionFactory);
        return transactionManager;
    }

    @Bean()
    CamelContextConfiguration contextConfiguration(
            @Autowired ConnectionFactory connectionFactory,
            @Autowired PlatformTransactionManager transactionManager)
    {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                JmsComponent jmsComponent = JmsComponent.jmsComponentTransacted(connectionFactory, transactionManager);
                // required for consumer-level redelivery after rollback
                jmsComponent.setCacheLevelName("CACHE_CONSUMER");
                jmsComponent.setTransacted(true);
                jmsComponent.getConfiguration().setConcurrentConsumers(1);
                camelContext.addComponent("activemq", jmsComponent);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // Do nothing
            }
        };
    }
}
