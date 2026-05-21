package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;
import com.group8.docprocessing.model.UploadQueueItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class DocumentEventConsumer {

    /**
     * Logger instance for logging events and errors.
     */
    private static final Logger log = LoggerFactory.getLogger(DocumentEventConsumer.class);
    
    /**
     * Service for performing OCR and data extraction.
     */
    private final OcrService ocrService;

    /**
     * Producer for sending processed document results to Kafka.
     */
    private final DocumentResultProducer documentResultProducer; 

    public DocumentEventConsumer(OcrService ocrService, DocumentResultProducer documentResultProducer) {
        this.ocrService = ocrService;
        this.documentResultProducer = documentResultProducer;
    }

    @KafkaListener(topics = "${doc_processing_kafka_topic_upload_event}", 
                   groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "uploadQueueItemKafkaListenerContainerFactory")
    public void handleUploadEvent(UploadQueueItem item, Acknowledgment acknowledgment) {
        log.info("Kafka Event Received for ID: {}", item.getProcessingId());

        // Set up for the reliability experiment: 
        // adds 5 second delay, so that there is enough time to shut down the service
        // try {
        //     Thread.sleep(5000);
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt();
        //     log.error("Thread was interrupted while sleeping", e);
        // }

        try {
            ExtractedData extractedData = ocrService.performOcrAndExtraction(
                item.getProcessingId(), 
                item.getGridFsId(),
                item.getUserId()
            );

            log.info("Extracted Data: Document Id {} for ID {} User ID: {}", 
                     extractedData.getDocumentId(), item.getProcessingId(), item.getUserId());

            documentResultProducer.sendProcessedEvent(extractedData);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Fatal error processing document ID: {}", item.getProcessingId(), e);
        }
    }
}