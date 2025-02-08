package com.example.demo.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.List;

@SpringBootApplication
public class EsDemoApp {

	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(EsDemoApp.class, args);

		DemoEvent demoEvent = new DemoEvent();
		demoEvent.setDetail("hello");

		DemoRepository demoRepository = applicationContext.getBean(DemoRepository.class);
		demoRepository.save(demoEvent);

		List<DemoEvent> savedEvents = demoRepository.findByDetail("hello");
		for (DemoEvent event:savedEvents) System.out.println(event.getId());

		ElasticsearchOperations elasticsearchOperations = applicationContext.getBean(ElasticsearchOperations.class);
		IndexOperations indexOperations = elasticsearchOperations.indexOps(DemoEvent.class);
		indexOperations.delete();
		boolean result = indexOperations.createWithMapping();

		applicationContext.close();
	}

}
