package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentResultProducerTest {

    /**
     * A mock KafkaTemplate used to simulate Kafka message production in tests.
     */
    @Mock
    private KafkaTemplate<String, ExtractedData> kafkaTemplate;

    /**
     * The DocumentResultProducer instance under test, with dependencies injected by Mockito.
     */
    @InjectMocks
    private DocumentResultProducer documentResultProducer;

    /**
     * The topic name used for testing Kafka message production.
     */
    private static final String TEST_TOPIC = "document_processed";

    /**
     * The test data object representing extracted data to be sent to Kafka.
     */
    private ExtractedData testData;
    /**
     * A unique identifier for the test data.
     */
    private String testId;

    /**
     * A CompletableFuture used to simulate the asynchronous behavior of Kafka send operations.
     */
    private CompletableFuture<SendResult<String, ExtractedData>> completableFuture;
    /**
     * A mock SendResult used to simulate the result of a Kafka send operation.
     */
    private SendResult<String, ExtractedData> mockSendResult;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentResultProducer, "processedTopic", TEST_TOPIC);

        testData = new ExtractedData();
        testId = UUID.randomUUID().toString();
        testData.setId(testId);
        testData.setDocumentId("TEST-456");

        mockSendResult = new SendResult<>(null, new RecordMetadata(null, 0, 0, 0, 0L, 0, 0));
        completableFuture = CompletableFuture.completedFuture(mockSendResult);
    }

    @Test
    void sendProcessedEventShouldCallKafkaTemplateSendWithCorrectArguments() {
        when(kafkaTemplate.send(eq(TEST_TOPIC), eq(testId), eq(testData)))
            .thenReturn(completableFuture);

        documentResultProducer.sendProcessedEvent(testData);

        verify(kafkaTemplate, times(1)).send(eq(TEST_TOPIC), eq(testId), eq(testData));
    }

    @Test
    void sendProcessedEventShouldHandleSuccessfulCompletion() {
        CompletableFuture<SendResult<String, ExtractedData>> futureSpy = spy(new CompletableFuture<>());
        
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(futureSpy);
        
        documentResultProducer.sendProcessedEvent(testData);

        verify(kafkaTemplate, times(1)).send(any(), any(), any());
        
        futureSpy.complete(mockSendResult);
    }

    @Test
    void sendProcessedEventShouldHandleFailureCompletion() {
        CompletableFuture<SendResult<String, ExtractedData>> futureSpy = spy(new CompletableFuture<>());
        RuntimeException mockException = new RuntimeException("Kafka unreachable");
        
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(futureSpy);
        
        documentResultProducer.sendProcessedEvent(testData);

        verify(kafkaTemplate, times(1)).send(any(), any(), any());
        
        futureSpy.completeExceptionally(mockException);
    }
}