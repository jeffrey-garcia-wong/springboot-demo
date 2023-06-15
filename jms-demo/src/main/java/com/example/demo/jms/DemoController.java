package com.example.demo.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    JmsTemplate jmsTemplate;

    @GetMapping("/send")
    public ResponseEntity<String> sendMessage() {
        jmsTemplate.convertAndSend("mailbox", "Hello " + System.nanoTime());
        return ResponseEntity.accepted().body("done");
    }

}
