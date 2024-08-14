package com.example.demo.couchbase.repository;

import com.couchbase.client.java.kv.MutationResult;
import com.example.demo.couchbase.entity.Item;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DemoRepositoryIT {

    @Autowired
    private DemoRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAllItems();
    }

    @Test
    void simulate_singleThreadUpsert_thenSuccess() {
        final String SOURCE_SYSTEM_ID = "system-1";
        final String SOURCE_ID = "source-1";
        final Item item = Item.builder()
                .itemId(String.join("-", SOURCE_SYSTEM_ID, SOURCE_ID))
                .sourceSystem(SOURCE_SYSTEM_ID)
                .sourceId(SOURCE_ID)
                .build();
        repository.upsert(item);
        assertThat(repository.findByItemId(String.join("-", SOURCE_SYSTEM_ID, SOURCE_ID))).isEqualTo(item);
    }

    @Test
    @DisplayName("if multi-threads concurrently upsert the same item, the upsert should complete in both operations without error")
    @SneakyThrows
    void simulate_multiThreadsConcurrentUpsert_thenSuccess() {
        final int MAX_THREADS = 50;
        final int UNIQUE_RECORD_COUNT = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

        final Callable<Set<Long>> upsertTask = () -> {
            final Set<Long> casSet = new HashSet<>();
            for (int i=0; i<UNIQUE_RECORD_COUNT; i++) {
                final String SOURCE_SYSTEM_ID = "system";
                final String SOURCE_ID = "source-" + i;
                final Item item = Item.builder()
                        .itemId(String.join("-", SOURCE_SYSTEM_ID, SOURCE_ID))
                        .sourceSystem(SOURCE_SYSTEM_ID)
                        .sourceId(SOURCE_ID)
                        .build();
                final Pair<MutationResult, Item> resultItemPair = repository.upsert(item);
                casSet.add(resultItemPair.getFirst().cas());
            }
            return casSet;
        };

        final List<Callable<Set<Long>>> callables = new LinkedList<>();
        for (int i=0; i<MAX_THREADS; i++) {
            callables.add(upsertTask);
        }

        final List<Future<Set<Long>>> futures = executorService.invokeAll(callables);
        futures.forEach(future -> {
            try {
                assertThat(future.get()).hasSize(UNIQUE_RECORD_COUNT);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(repository.findAllItems()).hasSize(UNIQUE_RECORD_COUNT);
    }

}
