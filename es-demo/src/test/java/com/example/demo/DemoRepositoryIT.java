package com.example.demo;

import org.apache.el.util.ExceptionUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DemoRepositoryIT {

    @Autowired
    DemoRepository demoRepository;

    @Autowired
    ElasticsearchOperations elasticsearchOperations;

    @Execution(ExecutionMode.CONCURRENT)
    @Test
    public void test_001() throws InterruptedException, ExecutionException {
        test();
    }

    @Execution(ExecutionMode.CONCURRENT)
    @Test
    public void test_002() throws InterruptedException, ExecutionException {
        test();
    }

    @Execution(ExecutionMode.CONCURRENT)
    @Test
    public void test_003() throws InterruptedException, ExecutionException {
        test();
    }

    @Execution(ExecutionMode.CONCURRENT)
    @Test
    public void test_004() throws InterruptedException, ExecutionException {
        test();
    }

    public void test() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Callable<Void>> callables = new LinkedList<>();
        for (int i=0; i<50; i++) {
            callables.add(() -> {
                try {
//                    elasticsearchOperations.indexOps(IndexCoordinates.of("log")).delete();
                    DemoEvent demoEvent = new DemoEvent();
                    demoEvent.setDetail("hello");
                    demoRepository.save(demoEvent);
//                    elasticsearchOperations.indexOps(IndexCoordinates.of("log")).create();

                    Optional<DemoEvent> savedDemoEvent = demoRepository.findById(demoEvent.getId());
                    assertTrue(savedDemoEvent.isPresent());
                    demoRepository.save(savedDemoEvent.get());
                    demoRepository.deleteById("savedDemoEvent.get().getid()");

//                    DemoEvent tamperedDemoEvent = new DemoEvent();
//                    tamperedDemoEvent.setDetail(savedDemoEvent.get().getId());
//                    tamperedDemoEvent.setId("tampered");
//                    demoRepository.save(tamperedDemoEvent);
//                    elasticsearchOperations.indexOps(IndexCoordinates.of("log")).delete();
//                    demoRepository.deleteById(savedDemoEvent.get().getId());

                    return null;
                } catch (RuntimeException e) {
                    throw e;
                }
            });
        }

        Collection<Future<Void>> futures = executorService.invokeAll(callables);
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
//                e.printStackTrace();
                Throwable rootCause = getRootCause(e);
                assertNotNull(rootCause);
                assertEquals(ResponseException.class, rootCause.getClass());
                ResponseException responseException = (ResponseException) rootCause;

//                assertEquals(DataAccessResourceFailureException.class, e.getCause().getClass());
//                DataAccessResourceFailureException dataAccessResourceFailureException = (DataAccessResourceFailureException) e.getCause();
//                assertEquals(RuntimeException.class, dataAccessResourceFailureException.getCause().getClass());
//                RuntimeException runtimeException = (RuntimeException) dataAccessResourceFailureException.getCause();
//                assertEquals(ResponseException.class, runtimeException.getCause().getClass());
//                ResponseException responseException = (ResponseException) runtimeException.getCause();

                Response response = responseException.getResponse();
                int httpStatusCode = response.getStatusLine().getStatusCode();
                String httpReasonPhrase = response.getStatusLine().getReasonPhrase();
                assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), httpStatusCode);
                assertEquals(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(), httpReasonPhrase);
            }
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            Thread.sleep(1000);
        }
        executorService.shutdownNow();
    }

    @Nullable
    private Throwable getRootCause(Throwable throwable) {
        if (throwable == null) return null;
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

}
