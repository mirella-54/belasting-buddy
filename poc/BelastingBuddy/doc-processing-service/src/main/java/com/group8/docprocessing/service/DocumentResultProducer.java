package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentResultProducer {

    /**
     * Logger instance for logging events and errors.
     */
    private static final Logger log = LoggerFactory.getLogger(DocumentResultProducer.class);
    
    /**
     * The name of the Kafka topic to which processed document events are sent.
     * This value is injected from the application properties.
     */
    @Value("${doc-processing.kafka.topic.processed-event}")
    private String processedTopic;

    /**
     * KafkaTemplate for sending messages to Kafka topics.
     */
    private final KafkaTemplate<String, ExtractedData> kafkaTemplate;

    public DocumentResultProducer(KafkaTemplate<String, ExtractedData> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends the extracted data to the 'document_processed' topic.
     * @param extractedData The result object from the OCR service.
     */
    public void sendProcessedEvent(ExtractedData extractedData) {
        String key = extractedData.getId().toString();
        
        kafkaTemplate.send(processedTopic, key, extractedData)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent processed event for Document ID: {} to topic {}", 
                             key, processedTopic);
                } else {
                    log.error("Failed to send processed event for Document ID: {} to topic {}. Error: {}", 
                              key, processedTopic, ex.getMessage());
                }
            });
    }
}