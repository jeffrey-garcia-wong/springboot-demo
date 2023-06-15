package com.example.demo.es;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.fail;

public class DemoServiceTests {

    private final DemoService demoService = new DemoService();

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(100)
    public void simpleDateFormat() {
        try {
            String output = demoService.simpleDateFormat("2021-12-22T17:13:12.1230001Z");
            System.out.println(output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(100)
    public void dateTimeFormat() {
        try {
            String output = demoService.dateTimeFormat("2021-12-22T17:13:12.1230001Z");
            System.out.println(output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
