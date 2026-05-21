package com.group8.docprocessing.service;

import com.group8.docprocessing.model.UploadQueueItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTest {

    /**
     * A mock KafkaTemplate used for testing Kafka message sending.
     */
    @Mock
    private KafkaTemplate<String, UploadQueueItem> kafkaTemplate;

    /**
     * A mock GridFsStorageService used for testing file storage operations.
     */
    @Mock
    private GridFsStorageService storageService;

    /**
     * A mock MultipartFile used for testing file uploads.
     */
    @Mock
    private MultipartFile mockFile;

    /**
     * The service under test, with dependencies mocked.
     */
    @InjectMocks
    private DocumentProcessingService documentProcessingService;

    /**
     * The Kafka topic used for testing upload functionality.
     */
    private static final String TEST_UPLOAD_TOPIC = "test_upload_topic";

    /**
     * A mock GridFS ID used for testing file storage.
     */
    private static final String MOCK_GRIDFS_ID = "654321fedcba";

    @BeforeEach
    void setUp() {
        // Inject the @Value field using reflection for unit testing
        ReflectionTestUtils.setField(documentProcessingService, "uploadTopic", TEST_UPLOAD_TOPIC);
    }

    @Test
    void processUploadShouldSucceedAndSendKafkaMessage() throws IOException {
        String testFilename = "invoice.pdf";
        String testContentType = "application/pdf";

        when(mockFile.getOriginalFilename()).thenReturn(testFilename);
        when(mockFile.getContentType()).thenReturn(testContentType);
        when(storageService.storeFile(eq(mockFile), any(String.class))).thenReturn(MOCK_GRIDFS_ID);

        String processingId = documentProcessingService.processUpload(mockFile, "test-metadata", "test-user-id");

        assertNotNull(processingId);
        assertTrue(processingId.startsWith("PROC-"));

        verify(storageService, times(1)).storeFile(eq(mockFile), eq(processingId));

        verify(kafkaTemplate, times(1)).send(
            eq(TEST_UPLOAD_TOPIC),
            eq(processingId),
            argThat(item -> item.getGridFsId().equals(MOCK_GRIDFS_ID) &&
                            item.getFileName().equals(testFilename) &&
                            item.getContentType().equals(testContentType))
        );
    }

    @Test
    void processUploadShouldThrowResponseStatusExceptionOnStorageFailure() throws IOException {
        when(storageService.storeFile(any(MultipartFile.class), any(String.class)))
            .thenThrow(new IOException("Disk failure"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            documentProcessingService.processUpload(mockFile, "test-metadata", "test-user-id");
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(storageService, times(1)).storeFile(any(MultipartFile.class), any(String.class));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void processUploadShouldHandleKafkaSendFailure() throws IOException {
        when(storageService.storeFile(any(MultipartFile.class), any(String.class))).thenReturn(MOCK_GRIDFS_ID);
        doThrow(new RuntimeException("Kafka unreachable")).when(kafkaTemplate).send(any(), any(), any());

        String processingId = documentProcessingService.processUpload(mockFile, "test-metadata", "test-user-id");

        assertNotNull(processingId);
        verify(storageService, times(1)).storeFile(any(MultipartFile.class), any(String.class));
        verify(kafkaTemplate, times(1)).send(any(), any(), any());
    }
}