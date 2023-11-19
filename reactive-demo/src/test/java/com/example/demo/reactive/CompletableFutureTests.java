package com.example.demo.reactive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class CompletableFutureTests {

    @DisplayName("A test which demonstrate how future can block the main thread")
    @RepeatedTest(10)
    public void verifyFutureIsBlockingMain() throws ExecutionException, InterruptedException {
        final AtomicReference<String> result = new AtomicReference<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        final Future<Void> future = executorService.submit(() -> {
            System.out.println("Executing in " + Thread.currentThread().getName());
            result.set("Hello World!");
            return null;
        });
        // must block the main thread to retrieve result
        future.get();

        System.out.println("Executing in " + Thread.currentThread().getName());
        // this assert will always succeed proving that main thread must be blocked
        assertTrue(future.isDone());
        assertEquals("Hello World!", result.get());
    }

    @DisplayName("A test which demonstrate how completable future is not blocking the main thread")
    @RepeatedTest(10)
    public void verifyCompletableFutureIsNotBlockingMain() throws InterruptedException {
        final AtomicReference<String> result = new AtomicReference<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        final CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Executing in " + Thread.currentThread().getName());
            return "Hello World!";
        }, executorService).thenAccept(s -> {
            // result is `pushed` back to the thread as it becomes available
            // main thread is not necessarily to be blocked to retrieve the result
            System.out.println("Executing in " + Thread.currentThread().getName());
            result.set(s);
        });
        // this assert will fail sporadically which proves the main thread is not blocked
        assertFalse(completableFuture.isDone());
        assertEquals("Hello World!", result.get());
    }
}
