package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class DemoEventTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    DemoEvent demoEvent = new DemoEvent();

    @Test
    public void test01() {
        DemoEvent clone = demoEvent.clone();
        clone.setId("1");
        clone.setDetail("testing");

        try {
            String jsonStr = objectMapper.writeValueAsString(clone);
            objectMapper.readValue(jsonStr, DemoEvent.class);
        } catch (JsonProcessingException e) {
            fail(e.getMessage());
        }
    }

}
