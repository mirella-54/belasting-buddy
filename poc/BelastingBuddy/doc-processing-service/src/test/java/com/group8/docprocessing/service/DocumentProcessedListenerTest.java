package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.Acknowledgment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentProcessedListenerTest {

    @Mock
    private GridFsStorageService storageService;

    @Mock
    private DocumentStorageClient storageClient;

    @InjectMocks
    private DocumentProcessedListener listener;

    @Mock
    private Acknowledgment acknowledgment;

    private ExtractedData mockEventData;
    
    private final String testProcessingId = UUID.randomUUID().toString();
    private final String testDocumentId = "DOC-12345";
    private final String testContent = "Extracted text content.";
    private final String testFileName = "test_document.pdf";
    private final byte[] testFileBytes = "file binary content".getBytes();


    @BeforeEach
    void setUp() {
        mockEventData = mock(ExtractedData.class);
        lenient().when(mockEventData.getId()).thenReturn(testProcessingId);
        lenient().when(mockEventData.getDocumentId()).thenReturn(testDocumentId);
        lenient().when(mockEventData.getExtractedContent()).thenReturn(testContent);
    }
    
    private InputStream createMockInputStream() {
        return new ByteArrayInputStream(testFileBytes);
    }

    @Test
    void testListen_Success() throws Exception {

        InputStream mockInputStream = createMockInputStream();
        
        when(storageService.getFileAsStream(testProcessingId)).thenReturn(mockInputStream);
        when(storageService.getFileName(testProcessingId)).thenReturn(testFileName);

        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);

        listener.listen(mockEventData, acknowledgment);

        verify(storageService, times(1)).getFileAsStream(testProcessingId);
        verify(storageService, times(1)).getFileName(testProcessingId);    }


    @Test
    void testListen_OriginalFileNotFound() throws Exception {
        when(storageService.getFileAsStream(testProcessingId)).thenReturn(null);

        listener.listen(mockEventData, acknowledgment);

        verify(storageService, times(1)).getFileAsStream(testProcessingId);
        verify(storageClient, never()).sendExtractedData(any(), any(), any(), any());
        verify(storageService, never()).deleteFile(anyString());

        verify(acknowledgment, never()).acknowledge();
    }
    
    @Test
    void testListen_FileDeletedEvenIfStorageClientFails() throws Exception {
        InputStream mockInputStream = createMockInputStream();

        when(storageService.getFileAsStream(testProcessingId)).thenReturn(mockInputStream);
        when(storageService.getFileName(testProcessingId)).thenReturn(testFileName);

        listener.listen(mockEventData, acknowledgment);

        verify(storageService, never()).deleteFile(testProcessingId); 

        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void testListen_ConversionToByteArrayFails() throws Exception {
        InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Simulated Read Error"));
        when(storageService.getFileAsStream(testProcessingId)).thenReturn(mockInputStream);

        listener.listen(mockEventData, acknowledgment);

        verify(storageClient, never()).sendExtractedData(any(), any(), any(), any());
        verify(storageService, never()).deleteFile(anyString());

        verify(acknowledgment, never()).acknowledge();
    }
}