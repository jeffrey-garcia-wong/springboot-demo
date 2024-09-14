package com.example.demo.couchbase.service;

import com.example.demo.couchbase.entity.Item;
import com.example.demo.couchbase.repository.DemoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoService {

    private final DemoRepository demoRepository;

    public DemoService(DemoRepository demoRepository) {
        this.demoRepository = demoRepository;
    }

    public Item upsert(Item item) {
        return demoRepository.upsert(item).getSecond();
    }

    public List<Item> getAll() {
        return demoRepository.findAllItems();
    }
}
