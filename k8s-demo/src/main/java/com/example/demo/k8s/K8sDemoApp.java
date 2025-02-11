package com.example.demo.k8s;

import com.example.demo.k8s.model.HttpBinData;
import com.example.demo.k8s.repository.DemoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class K8sDemoApp {
	private static final Logger LOGGER = LoggerFactory.getLogger(K8sDemoApp.class);

	public static void main(String[] args) {
		SpringApplication.run(K8sDemoApp.class, args);
	}

	@Bean
	CommandLineRunner runner(@Autowired DemoRepository repository) {
		return args -> {
            final List<HttpBinData> results = repository.findAll();
            LOGGER.info("results: {}", results);
        };
	}

}
