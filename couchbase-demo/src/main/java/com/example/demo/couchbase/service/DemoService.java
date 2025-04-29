package com.example.demo.couchbase.service;

import com.example.demo.couchbase.entity.Item;
import com.example.demo.couchbase.repository.DemoRepositoryImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoService {

    private final DemoRepositoryImpl demoRepositoryImpl;

    public DemoService(DemoRepositoryImpl demoRepositoryImpl) {
        this.demoRepositoryImpl = demoRepositoryImpl;
    }

    public Item upsert(Item item) {
        return demoRepositoryImpl.upsert(item).getSecond();
    }

    public List<Item> getAll() {
        return demoRepositoryImpl.findAllItems();
    }
}
