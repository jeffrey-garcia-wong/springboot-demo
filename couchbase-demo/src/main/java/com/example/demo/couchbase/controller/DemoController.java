package com.example.demo.couchbase.controller;

import com.example.demo.couchbase.entity.Item;
import com.example.demo.couchbase.service.DemoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController(value = "demoApi")
@RequestMapping(path = "/api/v1/demo")
public class DemoController {
    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @Autowired
    ObjectMapper objectMapper;

    @RequestMapping(value = "/upsert")
    public ResponseEntity<Item> upsert() throws Exception {
        final String SOURCE_SYSTEM_ID = "system-1";
        final String SOURCE_ID = "source-1";
        final Item item = Item.builder()
                .itemId(String.join("-", SOURCE_SYSTEM_ID, SOURCE_ID))
                .sourceSystem(SOURCE_SYSTEM_ID)
                .sourceId(SOURCE_ID)
                .zonedDateTime(
                        ZonedDateTime.of(
                                LocalDate.now(),
                                LocalTime.now().truncatedTo(ChronoUnit.SECONDS),
                                ZoneOffset.UTC
                        )
                )
                .build();
        return ResponseEntity.ok(demoService.upsert(item));
    }

    @RequestMapping(value = "/get")
    public ResponseEntity<List<Item>> get() {
        return ResponseEntity.ok(demoService.getAll());
    }

    @RequestMapping(value = "/post")
    public ResponseEntity<Item> post() {
        int id = new Random().nextInt(100);
        final String SOURCE_SYSTEM_ID = "system-" + id;
        final String SOURCE_ID = "source-" + id;
        final Item item = Item.builder()
                .itemId(String.join("-", SOURCE_SYSTEM_ID, SOURCE_ID))
                .sourceSystem(SOURCE_SYSTEM_ID)
                .sourceId(SOURCE_ID)
                .zonedDateTime(
                        ZonedDateTime.of(
                                LocalDate.now(),
                                LocalTime.now().truncatedTo(ChronoUnit.SECONDS),
                                ZoneOffset.UTC
                        )
                )
                .build();
        return ResponseEntity.ok(demoService.upsert(item));
    }

}
