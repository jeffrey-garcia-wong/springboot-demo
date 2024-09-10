package com.example.demo.kafka;

import com.example.demo.kafka.client.DemoClientApp;
import com.example.demo.kafka.kstream.DemoKStreamApp;
import com.example.demo.kafka.multikstream.DemoMultiKStreamApp;
import com.example.demo.kafka.reactive.DemoReactiveApp;
import org.springframework.boot.SpringApplication;

public class DemoKafkaApplication {
    public static void main(String [] args) {
        final String activeSpringProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        switch (activeSpringProfile) {
            case "client": {
                SpringApplication.run(DemoClientApp.class);
                break;
            }
            case "kstream": {
                SpringApplication.run(DemoKStreamApp.class);
                break;
            }
            case "reactive": {
                SpringApplication.run(DemoReactiveApp.class);
                break;
            }
            case "multi-kstream": {
                SpringApplication.run(DemoMultiKStreamApp.class);
                break;
            }
            default:
                throw new IllegalArgumentException("un-supported profile");
        }

    }
}
