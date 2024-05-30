package org.springframework.boot.autoconfigure.jms.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

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
@FunctionalInterface
public interface ActiveMQConnectionFactoryCustomizer {

	/**
	 * Customize the {@link ActiveMQConnectionFactory}.
	 * @param factory the factory to customize
	 */
	void customize(ActiveMQConnectionFactory factory);

}
