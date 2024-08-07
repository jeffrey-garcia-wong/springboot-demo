package com.exmple.demo.debezium;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DemoDebeziumApp {

    private final Serde<String> keySerde = Serdes.String();
    private final Serde<String> valueSerde = Serdes.String();

    public static void main(String[] args) throws IOException {
        new DemoDebeziumApp().run();
    }

    private Topology topology;
    private KafkaStreams streams;

    private void run() throws IOException {
        final Properties props = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("dev.properties")) {
            props.load(inputStream);
        }
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, props.getProperty("application.id"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("enable.idempotence", "true");
//        props.put("transactional.id", "tx");

        topology = this.buildTopology(props);
        //dynamically create topic
        this.createTopics(props);

        streams = new KafkaStreams(topology, props);

        // Attach shutdown handler to catch Control-C.
        final CountDownLatch latch = new CountDownLatch(1); // block until process is killed
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close(Duration.ofSeconds(5));
                latch.countDown();
            }
        });
        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private Topology buildTopology(Properties props) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String inputTopic = props.getProperty("input.topic.name");
        final String outputTopic = props.getProperty("output.topic.name");
        final String userTopic = props.getProperty("user.topic.name");

        // start monitoring user stream
        KStream<String, String> userStream =
                builder.stream(userTopic, Consumed.with(keySerde, valueSerde))
                        .peek((k,v) -> {
                            System.out.println("User: Key: " + k + " Value: " + v);
                        });

        // group user stream having associating with the same transaction id in the value and
        // output the result to a state store
        KGroupedStream<String,String> userStreamGroupByValue =
                userStream.groupBy((k,v) -> v, Grouped.with(Serdes.String(), Serdes.String()));
        userStreamGroupByValue.aggregate(
                () -> "",
                (key,value,aggregator) -> {
                    return aggregator + " " + value;
                },
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("user-group-table-01")
        );

        // start monitoring input stream
        KStream<String, String> inputStream =
                builder.stream(inputTopic, Consumed.with(keySerde, valueSerde))
                        .peek((k,v) -> {
                            System.out.println("Input: Key: " + k + " Value: " + v);
                        });

        inputStream.process(() -> (Processor<String, String, String, String>) record -> {
            System.out.println("searching record key from user: " + record.key());
            // searching user records associating with the input key
            final ReadOnlyKeyValueStore<String, String> keyValueStore =
                    streams.store(
                        StoreQueryParameters.fromNameAndType(
                                "user-group-table-01",
                                QueryableStoreTypes.keyValueStore()
                        )
                    );
            String users = keyValueStore.get(record.key());
            System.out.println("Users: " + users);

            // reorder user records based on its sequence in the transaction
            String [] userArray = users.split(" ");
            Arrays.sort(userArray);

            // atomically publish the result to output topic
            KafkaProducer<String, String> producer = new KafkaProducer<>(props);
            producer.initTransactions();
            try {
                producer.beginTransaction();
                for (String user : userArray) {
                    final ProducerRecord<String, String> producerRecord =
                            new ProducerRecord<>(outputTopic,
                                    user,
                                    user);
                    producer.send(producerRecord).get();
                }
                producer.commitTransaction();
            } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
                producer.close();
            } catch (KafkaException e) {
                producer.abortTransaction();
            } catch (final InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            producer.close();
        });

        return builder.build();
    }

    private void createTopics(Properties allProps) {
        final AdminClient client = AdminClient.create(allProps);
        final List<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic(
                allProps.getProperty("input.topic.name"),
                Integer.parseInt(allProps.getProperty("input.topic.partitions")),
                Short.parseShort(allProps.getProperty("input.topic.replication.factor"))));
        topics.add(new NewTopic(
                allProps.getProperty("output.topic.name"),
                Integer.parseInt(allProps.getProperty("output.topic.partitions")),
                Short.parseShort(allProps.getProperty("output.topic.replication.factor"))));
        topics.add(new NewTopic(
                allProps.getProperty("user.topic.name"),
                Integer.parseInt(allProps.getProperty("user.topic.partitions")),
                Short.parseShort(allProps.getProperty("user.topic.replication.factor"))));
        client.createTopics(topics);
        client.close();
    }

}