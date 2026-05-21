package com.group8.docprocessing.service;

import com.group8.docprocessing.model.UploadQueueItem; 
import java.io.IOException; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentProcessingService {

    /** 
     * Logger instance for logging events and errors.
     */
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class); 

    /**
     * KafkaTemplate for sending messages to Kafka topics.
     */
    private final KafkaTemplate<String, UploadQueueItem> kafkaTemplate; 

    /**
     * Service for storing files in GridFS.
     */
    private final GridFsStorageService storageService; 

    /**
     * The name of the Kafka topic to which upload events are sent.
     */
    @Value("${doc-processing.kafka.topic.upload-event}")
    private String uploadTopic; 

    public DocumentProcessingService(
            KafkaTemplate<String, UploadQueueItem> kafkaTemplate, 
            GridFsStorageService storageService 
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.storageService = storageService; 
    }

    /**
     * Handles the document upload process:
     * 1. Saves the file to GridFS.
     * 2. Creates an event payload with the storage ID.
     * 3. Sends the message to Kafka.
     *
     * @param file The uploaded file.
     * @param metadata Additional metadata (if any).
     * @param userId The ID of the user uploading the document.
     * @return The processing ID for tracking.
     */
    public String processUpload(MultipartFile file, String metadata, String userId) {
        String processingId = "PROC-" + System.currentTimeMillis(); 
        String gridFsId;

        try {
            gridFsId = storageService.storeFile(file, processingId); 
        } catch (IOException e) {
            log.error("Failed to save file to GridFS for ID: {}", processingId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store document.");
        }

        UploadQueueItem eventPayload = new UploadQueueItem(
                processingId, 
                file.getOriginalFilename(), 
                file.getContentType(), 
                gridFsId,
                userId
        );

        log.info("Publishing document upload event for ID: {} with GridFS ID: {} and User ID: {} to topic {}", 
            processingId, gridFsId, userId, uploadTopic);

        try {
            kafkaTemplate.send(uploadTopic, processingId, eventPayload);
            log.info("Successfully initiated Kafka send for ID: {}", processingId);
        } catch (Exception e) {
            log.error("Failed to send Kafka message for ID: {}", processingId, e);
        }

        return processingId;
    }
}