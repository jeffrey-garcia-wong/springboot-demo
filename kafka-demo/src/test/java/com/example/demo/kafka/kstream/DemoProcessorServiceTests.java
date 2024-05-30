package com.example.demo.kafka.kstream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DemoProcessorServiceTests {
    private TopologyTestDriver testDriver;
    private TestInputTopic<Long, String> inputTopic;
    private TestOutputTopic<Long, String> outputTopic;
    private String inputTopicName = "test-input-topic";
    private String outputTopicName = "test-output-topic";
    private String stateStoreName = "test-state-store";

    @BeforeEach
    public void init() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$LongSerde");
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        DemoProcessorService processorService = new DemoProcessorService(
                streamsBuilder,
                inputTopicName,
                outputTopicName,
                stateStoreName
        );

        testDriver = new TopologyTestDriver(
                processorService.startKStream(), props);

        inputTopic = testDriver.createInputTopic(
                inputTopicName,
                Serdes.Long().serializer(),
                Serdes.String().serializer()
        );

        outputTopic = testDriver.createOutputTopic(
                outputTopicName,
                Serdes.Long().deserializer(),
                Serdes.String().deserializer()
        );
    }

    @AfterEach
    public void tearDown() {
        testDriver.close();
    }

    @DisplayName("verify all unique value should be produced to output topic")
    @Test
    public void verifyInputUniqueValue() {
        final int totalUniqueRecord = 10;
        final List<TestRecord<Long, String>> testRecords = new LinkedList<>();
        for (int i = 0; i < totalUniqueRecord; i++) {
            inputTopic.pipeInput(
                    Long.valueOf(i),
                    String.valueOf(i)
            );
        }
        inputTopic.pipeRecordList(testRecords);

        // verify output topic
        final List<String> outputList = outputTopic.readValuesToList();
        assertEquals(totalUniqueRecord, outputList.size());

        // verify cache
        final KeyValueStore<Long, String> stateStore =
                testDriver.getKeyValueStore(stateStoreName);
        testRecords.forEach(testRecord -> {
            Long expectedKey = testRecord.getKey();
            String expectedValue = testRecord.getValue();
            String cachedValue = stateStore.get(expectedKey);
            assertEquals(expectedValue, cachedValue);
        });
    }

    @DisplayName("verify duplicate value should be filtered and only unique record produced to output topic")
    @Test
    public void verifyInputDuplicateValue() {
        final int duplicateRecordCount = 5;
        final List<TestRecord<Long, String>> testRecords = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            TestRecord<Long, String> testRecord = new TestRecord<>(
                    Long.valueOf(i % duplicateRecordCount),
                    String.valueOf(i % duplicateRecordCount)
            );
            testRecords.add(testRecord);
        }
        inputTopic.pipeRecordList(testRecords);

        // verify output topic
        final List<String> outputList = outputTopic.readValuesToList();
        assertEquals(duplicateRecordCount, outputList.size());

        // verify cache
        final KeyValueStore<Long, String> stateStore =
                testDriver.getKeyValueStore(stateStoreName);
        testRecords.forEach(testRecord -> {
            Long expectedKey = testRecord.getKey();
            String expectedValue = testRecord.getValue();
            String cachedValue = stateStore.get(expectedKey);
            assertEquals(expectedValue, cachedValue);
        });
    }
}
