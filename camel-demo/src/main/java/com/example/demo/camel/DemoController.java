package com.example.demo.camel;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    @Autowired
    ProducerTemplate producerTemplate;

    @GetMapping("/send")
    public ResponseEntity<String> sendMessage() {
        producerTemplate.sendBodyAndHeaders("direct:test", "hello world", Map.of());
        return ResponseEntity.accepted().body("done");
    }

}
