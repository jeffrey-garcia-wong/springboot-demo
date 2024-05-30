package org.springframework.boot.autoconfigure.jms.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import javax.jms.ConnectionFactory;

/**
 * <p>Manual restoration of ActiveMQ's autoconfiguration from spring-boot-autoconfigure
 * since its removal from Spring-Boot 3.x</p>
 *
 * @TODO
 * <p>Remove this when both of the following conditions are met:</p>
 * <ul>
 *     <li>Spring-Boot 3.x provides official support to ActiveMQ</li>
 *     <li>Apache Camel Spring-Boot Starter becomes compatible with Spring-Boot 3.x</li>
 * </ul>
 *
 * @version spring-boot-autoconfigure:2.7.9
 *
 * @see
 * <a href="https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#other-removals">ActiveMQ Removal</a>
 */
@AutoConfiguration(before = JmsAutoConfiguration.class, after = JndiConnectionFactoryAutoConfiguration.class)
@ConditionalOnClass({ ConnectionFactory.class, ActiveMQConnectionFactory.class })
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties({ ActiveMQProperties.class, JmsProperties.class })
@Import({ ActiveMQConnectionFactoryConfiguration.class })
public class ActiveMQAutoConfiguration {

}
