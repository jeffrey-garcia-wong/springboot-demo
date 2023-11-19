package com.example.demo.kafka.reactive;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderResult;
import reactor.kafka.sender.TransactionManager;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DemoReactiveProcessorTests {

    private ConsumerRecord<Long, String> createConsumerRecord() {
        final ConsumerRecord<Long, String> consumerRecord = new ConsumerRecord<>(
                "input-topic",
                0,
                0L,
                12345678L,
                "Hello World"
        );
        return consumerRecord;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void verifyConsumeMessageSuccessfully() {
        final Flux<ConsumerRecord<Long, String>> consumerRecordFlux =
                Flux.just(createConsumerRecord());

        final SenderResult<Long> mockSenderResult = mock(SenderResult.class);
        when(mockSenderResult.exception()).thenReturn(null);

        final TransactionManager mockTxManager = mock(TransactionManager.class);
        when(mockTxManager.commit()).thenReturn(Mono.just(mockSenderResult));

        final KafkaReceiver<Long,String> mockKafkaReceiver = mock(KafkaReceiver.class);
        when(mockKafkaReceiver.receiveExactlyOnce(any(TransactionManager.class)))
                .thenReturn(Flux.just(consumerRecordFlux));

        final KafkaSender<Long,String> mockKafkaSender = mock(KafkaSender.class);
        when(mockKafkaSender.transactionManager()).thenReturn(mockTxManager);

        final DemoTransformer mockDemoTransformer = mock(DemoTransformer.class);
        when(mockDemoTransformer.doTransform())
                .thenReturn(List.of("aaa"));

        final int maxRetryAttempt = 1;
        final DemoReactiveProcessor processor = new DemoReactiveProcessor(
                mockKafkaReceiver,
                mockKafkaSender,
                mockDemoTransformer,
                "output-topic",
                maxRetryAttempt,
                1000L
        );

        // simulate sending the output of transformed data
        doAnswer(invocation -> {
            return processor.transform(createConsumerRecord())
                    .flatMap(senderRecord -> {
                        return Flux.just(mockSenderResult);
                    });
        }).when(mockKafkaSender).send(any(CorePublisher.class));

        final Flux<SenderResult<Long>> senderResultFlux = processor.process();
        final Duration duration = StepVerifier.create(senderResultFlux.log())
                .expectNext(mockSenderResult) // first attempt
                .expectNext(mockSenderResult) // transaction commit
                .verifyComplete();

        final int totalAttempt = 1; // no retry
        verify(mockKafkaSender, times(totalAttempt))
                .send(any(CorePublisher.class));
        verify(mockDemoTransformer, times(1))
                .doTransform();
        verify(mockTxManager, times(totalAttempt))
                .commit();
    }

    @SuppressWarnings({"unchecked", "unused"})
    @Test
    public void verifyConsumeMessageTransformException() {
        final Flux<ConsumerRecord<Long, String>> consumerRecordFlux =
                Flux.just(createConsumerRecord());

        final TransactionManager mockTxManager = mock(TransactionManager.class);
        when(mockTxManager.abort()).thenReturn(Mono.just(new Object()));

        final KafkaReceiver<Long, String> mockKafkaReceiver = mock(KafkaReceiver.class);
        when(mockKafkaReceiver.receiveExactlyOnce(any(TransactionManager.class))).thenReturn(Flux.just(consumerRecordFlux));

        final KafkaSender<Long, String> mockKafkaSender = mock(KafkaSender.class);
        when(mockKafkaSender.transactionManager()).thenReturn(mockTxManager);

        final DemoTransformer mockDemoTransformer = mock(DemoTransformer.class);
        // simulate error during handler processing
        when(mockDemoTransformer.doTransform())
                .thenThrow(new RuntimeException("dummy-error"));

        final int maxRetryAttempt = 1;
        final DemoReactiveProcessor processor = new DemoReactiveProcessor(
                mockKafkaReceiver,
                mockKafkaSender,
                mockDemoTransformer,
                "output-topic",
                maxRetryAttempt,
                1000L
        );

        doAnswer(invocation -> {
            ConsumerRecord<Long,String> consumerRecord = createConsumerRecord();
            return processor.transform(consumerRecord);
        }).when(mockKafkaSender).send(any(CorePublisher.class));

        final Flux<SenderResult<Long>> senderResultFlux = processor.process();
        final Duration duration = StepVerifier.create(senderResultFlux.log())
                .expectNext() // first attempt fail
                .expectNext() // retry once and still fail
                .expectErrorMatches(e -> { // retry exhausted exception should be thrown
                    return e.getClass().getName().equals("reactor.core.Exceptions$RetryExhaustedException") &&
                            e.getMessage().equals("Retries exhausted: 1/1");
                }).verify();

        // total attempt = initial attempt + max retry attempt
        final int totalAttempt = 1 + maxRetryAttempt;
        verify(mockKafkaSender, times(totalAttempt))
                .send(any(CorePublisher.class));
        verify(mockDemoTransformer, times(totalAttempt))
                .doTransform();
        verify(mockTxManager, times(totalAttempt))
                .abort();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void verifyConsumeMessageSendException() {
        final Flux<ConsumerRecord<Long, String>> consumerRecordFlux = Flux.just(createConsumerRecord());

        final TransactionManager mockTxManager = mock(TransactionManager.class);
        when(mockTxManager.abort()).thenReturn(Mono.just(new Object()));

        final KafkaReceiver<Long, String> mockKafkaReceiver = mock(KafkaReceiver.class);
        when(mockKafkaReceiver.receiveExactlyOnce(any(TransactionManager.class))).thenReturn(Flux.just(consumerRecordFlux));

        final KafkaSender<Long, String> mockKafkaSender = mock(KafkaSender.class);
        when(mockKafkaSender.transactionManager()).thenReturn(mockTxManager);

        final DemoTransformer mockDemoTransformer = mock(DemoTransformer.class);
        when(mockDemoTransformer.doTransform())
                .thenReturn(List.of("aaa"));

        final int maxRetryAttempt = 1;
        final DemoReactiveProcessor processor = new DemoReactiveProcessor(
                mockKafkaReceiver,
                mockKafkaSender,
                mockDemoTransformer,
                "output-topic",
                maxRetryAttempt,
                1000L
        );

        // simulate error during kafka send
        doAnswer(invocation -> {
            ConsumerRecord<Long,String> consumerRecord = createConsumerRecord();
            processor.transform(consumerRecord).subscribe();
            throw new RuntimeException("dummy-error");
        }).when(mockKafkaSender).send(any(CorePublisher.class));

        final Flux<SenderResult<Long>> senderResultFlux = processor.process();
        final Duration duration = StepVerifier.create(senderResultFlux.log())
                .expectNext() // first attempt fail
                .expectNext() // retry once and still fail
                .expectErrorMatches(e -> { // retry exhausted exception should be thrown
                    return e.getClass().getName().equals("reactor.core.Exceptions$RetryExhaustedException") &&
                            e.getMessage().equals("Retries exhausted: 1/1");
                }).verify();

        // total attempt = initial attempt + max retry attempt
        final int totalAttempt = 1 + maxRetryAttempt;
        verify(mockKafkaSender, times(totalAttempt))
                .send(any(CorePublisher.class));
        verify(mockDemoTransformer, times(totalAttempt))
                .doTransform();
        verify(mockTxManager, times(totalAttempt))
                .abort();
    }
}
