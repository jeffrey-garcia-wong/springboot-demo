package com.example.demo.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemoRepository extends ElasticsearchRepository<DemoEvent, String> {

    List<DemoEvent> findByDetail(String detail);
}
