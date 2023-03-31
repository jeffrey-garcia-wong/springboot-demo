package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	// isolate the elasticsearch data repository layer so the test can run without it
	"spring.elasticsearch.enabled = false",
})
class DemoApplicationIT {

	@Test
	void contextLoads() {

	}

}
