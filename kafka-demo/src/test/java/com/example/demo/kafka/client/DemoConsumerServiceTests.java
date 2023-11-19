package com.example.demo.kafka.client;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class DemoConsumerServiceTests {

    @SuppressWarnings("unchecked")
    @Test
    public void verifyConsumeMessage() throws ExecutionException, InterruptedException {
        final Long key = 12345678L;
        final String value = "Hello World";

        final CompletableFuture<SendResult<Long,String>> mockCompletableFuture =
                mock(CompletableFuture.class);

        final SendResult<Long, String> mockSendResult = mock(SendResult.class);
        when(mockCompletableFuture.get()).thenReturn(mockSendResult);

        final DemoProducerService mockProducerService = mock(DemoProducerService.class);
        when(mockProducerService
                .sendMessageAsync(anyLong(), anyString()))
                .thenReturn(mockCompletableFuture);

        final DemoStreamService mockStreamService = mock(DemoStreamService.class);
        when(mockStreamService.hasValueMutated(anyLong(), anyString())).thenReturn(true);

        final Acknowledgment mockAcknowledgement = mock(Acknowledgment.class);

        final DemoConsumerService consumerService = new DemoConsumerService(mockProducerService, mockStreamService);
        consumerService.consumeMessage(
                "input-topic",
                0,
                0L,
                key,
                value,
                mockAcknowledgement
        );

        verify(mockProducerService, times(1))
                .sendMessageAsync(anyLong(), anyString());
        verify(mockAcknowledgement, times(1))
                .acknowledge();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verifyConsumeMessage_exceptionThrownDuringSent() throws Exception {
        final Long key = 12345678L;
        final String value = "Hello World";

        final CompletableFuture<SendResult<Long,String>> mockCompletableFuture =
                mock(CompletableFuture.class);

        final SendResult<Long, String> mockSendResult = mock(SendResult.class);
        when(mockCompletableFuture.get()).thenThrow(
                new ExecutionException("send failure", new RuntimeException()));

        final DemoProducerService mockProducerService = mock(DemoProducerService.class);
        when(mockProducerService
                .sendMessageAsync(anyLong(), anyString()))
                .thenReturn(mockCompletableFuture);

        final DemoStreamService mockStreamService = mock(DemoStreamService.class);
        when(mockStreamService.hasValueMutated(anyLong(), anyString())).thenReturn(true);

        final Acknowledgment mockAcknowledgement = mock(Acknowledgment.class);

        final DemoConsumerService consumerService = new DemoConsumerService(mockProducerService, mockStreamService);
        consumerService.consumeMessage(
                "input-topic",
                0,
                0L,
                key,
                value,
                mockAcknowledgement
        );

        verify(mockProducerService, times(1))
                .sendMessageAsync(anyLong(), anyString());
        verify(mockAcknowledgement, times(1))
                .nack(any(Duration.class));
    }
}
