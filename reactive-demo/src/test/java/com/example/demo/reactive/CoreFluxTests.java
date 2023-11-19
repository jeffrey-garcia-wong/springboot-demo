package com.example.demo.reactive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoreFluxTests {

    @DisplayName("A test to verify that flatMap operator lose ordering of data in the stream")
    @RepeatedTest(10)
    public void verifyFlatMapOrdering() throws InterruptedException {
        final StringBuilder stringBuilder = new StringBuilder();
        final CountDownLatch latch = new CountDownLatch(1);
        Flux.just("a","b","c")
            .flatMap(s -> { // ordering of data is no longer guaranteed
                return Flux.just(s.toUpperCase()).delayElements(Duration.ofMillis(10));
            }).log().doOnNext(s -> {
                stringBuilder.append(s);
            }).doOnComplete(() -> {
                latch.countDown();
            }).subscribe();
        latch.await();

        final String result = stringBuilder.toString();
        // this assert will fail sporadically which proves flatMap lose ordering
        assertEquals("ABC", result);
    }

    @DisplayName("A test to verify that concatMap operator guarantee to preserve ordering of data in the stream")
    @RepeatedTest(10)
    public void verifyConcatMapOrdering() throws InterruptedException {
        final StringBuilder stringBuilder = new StringBuilder();
        final CountDownLatch latch = new CountDownLatch(1);
        Flux.just("a","b","c")
                .concatMap(s -> { // ordering of data is no longer guaranteed
                    return Flux.just(s.toUpperCase()).delayElements(Duration.ofMillis(10));
                }).log().doOnNext(s -> {
                    stringBuilder.append(s);
                }).doOnComplete(() -> {
                    latch.countDown();
                }).subscribe();
        latch.await();

        final String result = stringBuilder.toString();
        // this assert should always succeed which proves concatMap guarantee ordering
        assertEquals("ABC", result);
    }

    @Test
    public void verifyRetry() throws InterruptedException {
        Flux<Integer> flux = Flux.fromIterable(List.of(1,2,3))
                .map(i -> {
                    if ((i & 1) == 0) throw Exceptions.propagate(new RuntimeException("debug-error"));
                    return i;
                }).onErrorResume(e -> {
                    // handle the error and skip remaining in stream
                    return Flux.error(e);
                }).retryWhen(
                    Retry.backoff(1, Duration.ofMillis(1000L))
                ).doOnError(e -> {
                    // this happens when retry exhausted
                });
//        flux.subscribe();

        Duration d = StepVerifier.create(flux.log())
                .expectNext(1)
                .expectNext(1) // 1 retry attempt
                .expectErrorMatches(e -> { // retry exhausted exception should be thrown
                    return e.getClass().getName().equals("reactor.core.Exceptions$RetryExhaustedException") &&
                            e.getMessage().equals("Retries exhausted: 1/1");
                })
                .verify();
    }

}
