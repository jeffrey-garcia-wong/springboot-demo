package com.example.demo.kafka.client.perf;

import com.example.demo.kafka.client.DemoProducerService;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * <h1>JMH benchmark test for message producing into Kafka.</h1>
 *
 * This class uses JMH to benchmark the performance of the {@link DemoProducerService}.
 * Configure the connection parameters in /resources/application-cloud.yml for test to
 * connect with your target Kafka environment. This test overrides the producer's setting
 * configured in /resources/application.yml, applying Kafka's default producer's setting
 * to demonstrate how throughput performance behaves when 100000 messages are sent
 * asynchronously. The expected observation is that it produces a higher average score
 * due to the producer sent throughput limited by {@code spring.kafka.producer.batch-size=16384}.<p/>
 *
 * @see
 * <a href="https://developer.confluent.io/tutorials/optimize-producer-throughput/confluent.html">Optimize producer throughput</a> <br/>
 * <a href="https://docs.confluent.io/cloud/current/client-apps/optimizing/throughput.html#compression">Compression</a> <br/>
 *
 * @deprecated This JMH benchmark is now obsoleted.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@SuppressWarnings("unused")
@SpringBootTest(classes = {DemoProducerService_defaultBenchmarkTests.App.class})
@ActiveProfiles("client")
//@EnabledIf(expression = "#{environment['spring.profiles.active'] == 'client'}", loadContext = true)
@TestPropertySource(properties = {
        // override logging level in /resources/application-cloud.yml to reduce performance overheads
        "logging.level.com.example.demo.kafka=warn",
        // override producer throughput settings in /resources/application-client.yml with kafka's default
        "spring.kafka.producer.compression-type=none",
        "spring.kafka.producer.batch-size=16384",
        "spring.kafka.producer.properties.linger.ms=0",
        // provide dummy consumer's id so consumer can startup
        "spring.kafka.consumer.group-id=dummy-consumer"
})
public class DemoProducerService_defaultBenchmarkTests {
    private final static Logger LOGGER = LoggerFactory.getLogger(DemoProducerService_defaultBenchmarkTests.class);
    private final static Integer WARMUP_ITERATIONS = 3;
    private final static Integer MEASUREMENT_ITERATIONS = 10;

    @SpringBootApplication
    @Import(DemoProducerService.class)
    public static class App {}

    @Test
    public void executeJmhRunner() throws RunnerException {
        Options opt = new OptionsBuilder()
                // set the class name regex for benchmarks to search for to the current class
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .warmupIterations(WARMUP_ITERATIONS)
                .measurementIterations(MEASUREMENT_ITERATIONS)
                // do not use forking or the benchmark methods will not see references stored within its class
                .forks(0)
                // do not use multiple threads
                .threads(1)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.JSON)
                .result("target/jmh_" + this.getClass().getSimpleName() + "_results.json") // set this to a valid filename if you want reports
                .shouldFailOnError(true)
                .jvmArgs("-server")
                .build();
        new Runner(opt).run();
    }

    private static ConfigurableApplicationContext applicationContext;
    private static DemoProducerService producerService;

    /**
     * Use setter autowiring to make Spring save an instance of {@link ConfigurableApplicationContext}
     * into a static field accessible be the benchmarks spawned through the JMH runner.
     * @param applicationContext
     */
    @Autowired
    void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        DemoProducerService_defaultBenchmarkTests.applicationContext = applicationContext;
        DemoProducerService_defaultBenchmarkTests.producerService =
                applicationContext.getBean(DemoProducerService.class);
    }

    /**
     * Send 100000 messages asynchronously into kafka.<p/>
     *
     * @implNote
     * With kafka's default producer throughput settings,
     * the average score from this benchmark test is
     * expected to be larger comparing with the result
     * produced with the benchmark test using kafka's
     * recommended producer throughput settings.
     */
    @Benchmark
    public void benchmark_producer_sendBulkMessages() {
        try {
            for (int i=0; i<100000; i++) {
                producerService
                        .sendMessageAsync(Long.valueOf(i), String.valueOf(i))
                        .whenCompleteAsync((result, ex) -> {
                            if (ex != null) {
                                LOGGER.error(ex.getMessage(), ex);
                                fail("exception should not be thrown during send");
                            }
                        });
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            fail("exception should not be thrown before send");
        }
    }
}
