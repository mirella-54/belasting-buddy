package com.group8.docprocessing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.group8.docprocessing.model.ExtractedData;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

@Component
public class DocumentProcessedListener {

    /**
     * Logger instance for logging events and errors.
     */
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessedListener.class);

    /**
     * Service for interacting with GridFS storage.
     */
    private final GridFsStorageService storageService;

    /**
     * Client for communicating with the document storage service.
     */
    private final DocumentStorageClient storageClient;

    /**
     * Service for handling downstream communications.
     */
    private final DownstreamService downstreamService;

    public DocumentProcessedListener(GridFsStorageService storageService, DocumentStorageClient storageClient, 
        DownstreamService downstreamService) {
        this.storageService = storageService;
        this.storageClient = storageClient;
        this.downstreamService = downstreamService;
    }
    
    @KafkaListener(topics = "${doc-processing.kafka.topic.processed-event}", 
                   groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "extractedDataKafkaListenerContainerFactory")
    public void listen(ExtractedData eventData, Acknowledgment acknowledgment) { 

        String processingId = eventData.getId().toString();
        String documentId = eventData.getDocumentId();
        byte[] fileData = null;
        String fileName = null;

        log.info("Received Document Processed Event for Document ID: {} (Processing ID: {})", 
                 documentId, processingId);
        
        try (InputStream inputStream = storageService.getFileAsStream(processingId)) {
            
            if (inputStream == null) {
                log.warn("Original file not found in GridFS for processing ID: {}.", processingId);
                return; 
            }
            
            // 1. Convert InputStream to byte array
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            fileData = buffer.toByteArray();
            
            // 2. Get the original filename
            fileName = storageService.getFileName(processingId);
            if (fileName == null) {
                // Fallback to a constructed name if metadata is missing
                fileName = processingId + ".dat";
            }
            
            // --- Production Call ---
            log.info("Attempting to call doc-storage /upload endpoint (Production path)...");
            ResponseEntity<String> prodResponse = downstreamService.sendExtractedDataToStorage(
                eventData, fileData, fileName, "/upload"
            );
            log.info("Doc-storage /upload response: Status={}, Body={}", 
                     prodResponse.getStatusCode(), prodResponse.getBody());
            
            // --- Testing Call (For Stress Testing) ---
            /*
            log.info("Attempting to call doc-storage /upload-local endpoint (Testing path)...");
            
            ResponseEntity<String> testResponse = downstreamService.sendExtractedDataToStorage(
                eventData, fileData, fileName, "/upload-local"
            );
            log.info("Doc-storage /upload-local response: Status={}, Body={}", 
                     testResponse.getStatusCode(), testResponse.getBody());
            */
            
            
            // 3. Delete the file from the GridFS in the doc-processing-service
            log.info("Attempting to delete temporary file with Processing ID: {}", processingId);
            storageService.deleteFile(processingId);
            log.info("Successfully deleted temporary file with Processing ID: {}", processingId);

            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Fatal error processing Kafka event for Document ID: {}. Error details: {}", 
                      documentId, e.getMessage(), e);
        }
    }
}