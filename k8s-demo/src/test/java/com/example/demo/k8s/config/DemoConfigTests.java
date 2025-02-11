package com.example.demo.k8s.config;

import com.example.demo.k8s.props.DemoAppProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class DemoConfigTests {

    private AnnotationConfigApplicationContext applicationContext;

    @BeforeEach
    public void setUp() {
        this.applicationContext = new AnnotationConfigApplicationContext();
    }

    @AfterEach
    public void tearDown() {
        if (this.applicationContext != null) {
            this.applicationContext.close();
        }
    }

    @Test
    public void verifyRestTemplate() {
        applicationContext.register(DemoConfig.class);
        applicationContext.refresh();
        assertDoesNotThrow(() -> {
            applicationContext.getBean(RestTemplate.class);
        });

    }

    @Test
    public void verifyBaseUrl() {
        TestPropertyValues.of("app.config.api.baseUrl=https://httpbin.org")
                .applyTo(applicationContext);
        applicationContext.register(DemoConfig.class);
        applicationContext.refresh();

        assertDoesNotThrow(() -> {
            final DemoAppProperties appProperties =
                    applicationContext.getBean(DemoAppProperties.class);
            assertEquals(
                    "https://httpbin.org",
                    appProperties.getApi().getBaseUrl()
            );
        });
    }

}
