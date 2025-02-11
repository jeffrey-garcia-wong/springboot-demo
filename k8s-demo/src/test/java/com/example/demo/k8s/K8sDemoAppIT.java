package com.example.demo.k8s;

import com.example.demo.k8s.controller.DemoController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class K8sDemoAppIT {

	@Autowired
	ApplicationContext context;

	@Test
	void contextLoads() {
		context.getBean(DemoController.class);
	}

}
