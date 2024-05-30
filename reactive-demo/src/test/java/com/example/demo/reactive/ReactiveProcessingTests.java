package com.example.demo.reactive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class ReactiveProcessingTests {
    public Flux<Integer> consume(Integer i) {
        return Flux.just(i);
    }

    private Flux<String> api(int i) {
        return Flux.fromIterable(List.of("a","b","c"))
                .delayElements(Duration.ofMillis(100));
    }

    private CompletableFuture<String> produce(String s) {
        return CompletableFuture.supplyAsync(() -> {
            String out = "Hello: " + s;
            return out;
        });
    }

    @DisplayName("A test to verify data processing of consume, transform, produce in a reactive fashion")
    @Test
    public void verifyProcessingPipeline() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        consume(1)
                .flatMap(i -> {
                    return api(i);
                }).map(s -> {
                    return produce(s);
                }).doOnNext(cf -> {
                    cf.thenAccept(s -> {
                        System.out.println(s);
                    });
                }).doOnComplete(() -> {
                    latch.countDown();
                }).subscribe();
        latch.await();
    }
}
