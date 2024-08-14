package com.example.demo.couchbase.service;

import com.example.demo.couchbase.entity.Item;
import com.example.demo.couchbase.repository.DemoRepository;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    private final DemoRepository demoRepository;

    public DemoService(DemoRepository demoRepository) {
        this.demoRepository = demoRepository;
    }

    public Item upsert(Item item) {
        return demoRepository.upsert(item).getSecond();
    }

}
