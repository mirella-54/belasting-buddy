package com.group8.docprocessing.config;

import com.group8.docprocessing.model.ExtractedData;
import com.group8.docprocessing.model.UploadQueueItem;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    /**
     * The name of the Kafka bootstrap servers, sourced from application properties.
     */
    @Value("${SPRING_KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, UploadQueueItem> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
     
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false); 
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, UploadQueueItem> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // New ProducerFactory for ExtractedData
    @Bean
    public ProducerFactory<String, ExtractedData> extractedDataProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
     
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false); 
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ExtractedData> extractedDataKafkaTemplate() {
        return new KafkaTemplate<>(extractedDataProducerFactory());
    }
}