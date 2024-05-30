package com.example.demo.es;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * To run this benchmark, execute the following in command-line:
 * <pre>
 * {@code
 * ./gradlew :es-demo:jmh
 * }
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 10)
@Fork(value = 0)
@Threads(value = 1)
@SuppressWarnings("unused")
public class DemoServiceBenchmarkTests {

    private DemoService demoService;


    @Setup
    public void init() {
        demoService = new DemoService();
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
