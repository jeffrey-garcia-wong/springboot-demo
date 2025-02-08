package com.example.demo.jpa.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@Slf4j
@SpringBootApplication
public class DemoOracleJpaApp {
    public static void main(String[] args) {
        final SpringApplication application = new SpringApplication(DemoOracleJpaApp.class);
        application.setWebApplicationType(WebApplicationType.SERVLET);
        final ConfigurableApplicationContext applicationContext = application.run(args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(@Autowired DataSource dataSource) {
        return args -> {
            log.info("CommandLineRunner running...");
            log.info("data source: {}", dataSource);
        };
    }
}