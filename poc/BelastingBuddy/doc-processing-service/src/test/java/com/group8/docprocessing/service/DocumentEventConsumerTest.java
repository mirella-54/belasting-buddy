package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;
import com.group8.docprocessing.model.UploadQueueItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentEventConsumerTest {

    /**
     * Mocked OCR service for performing OCR and data extraction.
     */
    @Mock
    private OcrService ocrService;

    /**
     * Mocked producer for sending processed document events.
     */
    @Mock
    private DocumentResultProducer documentResultProducer;

    /**
     * The DocumentEventConsumer instance under test.
     */
    @InjectMocks
    private DocumentEventConsumer documentEventConsumer;

    /**
     * Test item representing an upload queue item for testing purposes.
     */
    private UploadQueueItem testItem;

    /**
     * Test data representing extracted data for testing purposes.
     */
    private ExtractedData testExtractedData;

    /**
     * Mocked Acknowledgment for manual commit testing.
     */
    @Mock 
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        testItem = new UploadQueueItem("PROC-12345", "testfile.pdf", "pdf", "gridfs-abc", "test-user-id");
        testExtractedData = new ExtractedData();
        testExtractedData.setId("PROC-12345");
        testExtractedData.setDocumentId("TEST-INV-1");
        testExtractedData.setUserId("test-user-id");
    }

    @Test
    void handleUploadEventShouldProcessAndSendResultOnSuccess() {
        when(ocrService.performOcrAndExtraction(testItem.getProcessingId(), testItem.getGridFsId(), testItem.getUserId()))
                .thenReturn(testExtractedData);

        documentEventConsumer.handleUploadEvent(testItem, acknowledgment);

        verify(ocrService, times(1)).performOcrAndExtraction(testItem.getProcessingId(), testItem.getGridFsId(), testItem.getUserId());
        verify(documentResultProducer, times(1)).sendProcessedEvent(testExtractedData);

        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void handleUploadEventCatchExceptionAndNotSendResultOnOcrFailure() {
        RuntimeException failure = new RuntimeException("OCR failed deliberately");
        
        when(ocrService.performOcrAndExtraction(testItem.getProcessingId(), testItem.getGridFsId(), testItem.getUserId()))
                .thenThrow(failure);

        documentEventConsumer.handleUploadEvent(testItem, acknowledgment);

        verify(ocrService, times(1)).performOcrAndExtraction(testItem.getProcessingId(), testItem.getGridFsId(), testItem.getUserId());
        verify(documentResultProducer, never()).sendProcessedEvent(any(ExtractedData.class));
        verify(acknowledgment, never()).acknowledge();
    }
}