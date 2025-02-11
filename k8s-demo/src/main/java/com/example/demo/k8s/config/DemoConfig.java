package com.example.demo.k8s.config;

import com.example.demo.k8s.props.DemoAppProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({DemoAppProperties.class})
@EnableCaching
public class DemoConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    ConcurrentMapCacheFactoryBean cache() {
        ConcurrentMapCacheFactoryBean cache = new ConcurrentMapCacheFactoryBean();
        cache.setName("httpbin");
        return cache;
    }

}
