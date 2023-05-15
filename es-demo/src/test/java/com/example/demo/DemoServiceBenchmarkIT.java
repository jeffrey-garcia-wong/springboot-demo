package com.example.demo;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@SpringBootTest(
        classes = DemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@TestPropertySource(properties = {
        // isolate the elasticsearch data repository layer so the test can run without it
        "spring.elasticsearch.enabled = false",
})
public class DemoServiceBenchmarkIT {

    @Test
    public void executeJmhRunner() throws RunnerException {
        Options opt = new OptionsBuilder()
                // set the class name regex for benchmarks to search for to the current class
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .warmupIterations(1)
                .measurementIterations(1)
                // do not use forking or the benchmark methods will not see references stored within its class
                .forks(0)
                // do not use multiple threads
                .threads(1)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.JSON)
                .result("/dev/null") // set this to a valid filename if you want reports
                .shouldFailOnError(true)
                .jvmArgs("-server")
                .build();

        new Runner(opt).run();
    }

    @Setup
    public void init() { }

    private static DemoService demoService;

    @Autowired
    public void setContext(ApplicationContext context) {
        DemoServiceBenchmarkIT.demoService = context.getBean(DemoService.class);
    }

    @Benchmark
    public void benchmarkDateTimeService_simpleDateFormat() throws Exception {
        demoService.simpleDateFormat("2021-12-22T17:13:12.1230001Z");
    }

    @Benchmark
    public void benchmarkDateTimeService_dateTimeFormat() throws Exception {
        demoService.dateTimeFormat("2021-12-22T17:13:12.1230001Z");
    }
}
