package com.example.demo.couchbase.controller;

import com.example.demo.couchbase.entity.Item;
import com.example.demo.couchbase.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController(value = "demoApi")
@RequestMapping(path = "/api/v1/demo")
public class DemoController {
    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @RequestMapping(value = "/upsert")
    public ResponseEntity<Item> upsert() {
        final String SOURCE_SYSTEM_ID = "system-1";
        final String SOURCE_ID = "source-1";
        final Item item = Item.builder()
                .itemId(String.join("-", SOURCE_SYSTEM_ID, SOURCE_ID))
                .sourceSystem(SOURCE_SYSTEM_ID)
                .sourceId(SOURCE_ID)
                .build();
        return ResponseEntity.ok(demoService.upsert(item));
    }

}
