package com.example.demo.k8s.controller;

import com.example.demo.k8s.props.DemoAppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DemoAppProperties appProperties;

    @Test
    void verifyGetWithPathVariable() {
        final ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/api/delay/1", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verifyGetWithRequestParam() {
        final ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/api/delay?waitInSec=1", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verifyPostWithJsonBody() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        final String jsonString = "{\"key1\":\"value1\"}";
        final ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/delay/1",
                HttpMethod.POST,
                new HttpEntity<>(jsonString, headers),
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verifyPostWithFormPost() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("key1", "value1");
        formData.add("key2", "value2");

        final ResponseEntity<String> response = restTemplate.exchange(
//                "http://localhost:" + port + "/api/delay/1",
                "https://httpbin.org/delay/1",
                HttpMethod.POST,
                new HttpEntity<>(formData, headers),
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verifyDeleteWithPathVariable() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/delay/1",
                HttpMethod.DELETE,
                null,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verifyPutWithPathVariable() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/delay/1",
                HttpMethod.PUT,
                null,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
