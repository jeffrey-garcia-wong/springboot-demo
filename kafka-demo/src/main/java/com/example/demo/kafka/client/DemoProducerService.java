package com.example.demo.kafka.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Service
public class DemoProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoProducerService.class);
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final String outputTopic;

    public DemoProducerService(
            @Autowired
            KafkaTemplate<Long, String> kafkaTemplate,
            @Value("${app.config.output-topic}")
            String outputTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.outputTopic = outputTopic;
    }

    /**
     * Send message to kafka topic asynchronously.<p/>
     *
     * @param key the key for partitioning the message
     * @param value the target object to send
     * @return a {@link CompletableFuture} of the {@link SendResult}
     *
     * @implNote
     * Caller of this method should define a callback to introspect the results
     * {@link CompletableFuture#whenCompleteAsync(BiConsumer)}. If the message
     * is successfully sent, the callback will be invoked with the {@link SendResult},
     * otherwise if message sending fails, there are 2 possible outcomes:
     * <ul>
     *     <li>
     *         Transient error, the callback will be invoked with the {@link Exception} object
     *         and logged. Retrying will be managed by kafka client, either the retries are
     *         exhausted or the delivery` timeout is reached, whichever comes first.
     *     </li>
     *     <li>
     *         Unrecoverable error, which happens before even sending, this usually indicates
     *         an error with the message itself (i.e. {@link org.apache.kafka.common.errors.SerializationException}),
     *         the exception is thrown before the callback is even invoked. In this case, the
     *         caller should wrap the invocation of this method with a try-catch block, and it's
     *         up to the caller to decide what to do with the exception, a general recommendation
     *         is that retry won't be useful if the error is due to the message itself.
     *     </li>
     * </ul>
     */
    public CompletableFuture<SendResult<Long, String>> sendMessageAsync(Long key, String value) {
        Objects.requireNonNull(key);
        return kafkaTemplate.send(outputTopic, key, value).handleAsync((result, ex) -> {
            if (ex != null) {
                LOGGER.error(ex.getMessage(), ex);
            } else {
                LOGGER.debug("Send result: {}", result);
            }
            return result;
        });
    }
}
