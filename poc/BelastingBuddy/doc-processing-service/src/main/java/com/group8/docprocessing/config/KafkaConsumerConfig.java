package com.group8.docprocessing.config;

import com.group8.docprocessing.model.ExtractedData;
import com.group8.docprocessing.model.UploadQueueItem;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    /**
    * The name of the Kafka bootstrap servers, sourced from application properties.
    */
    @Value("${SPRING_KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrapServers;

    /**
    * The Kafka consumer group ID, sourced from application properties.
    */
    @Value("${SPRING_KAFKA_CONSUMER_GROUP_ID}")
    private String groupId;

    /**
    * Defines the configuration properties for the Kafka consumer.
    * @return A map containing all consumer configuration properties.
    */
    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UploadQueueItem.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); 
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }

    /**
     * Creates the ConsumerFactory for UploadQueueItem.
     * @return The configured ConsumerFactory for String keys and UploadQueueItem values.
     */
    @Bean
    public ConsumerFactory<String, UploadQueueItem> uploadQueueItemConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            consumerConfig(), 
            new StringDeserializer(), 
            new JsonDeserializer<>(UploadQueueItem.class)
        );
    }

    /**
     * Creates the ConcurrentKafkaListenerContainerFactory for UploadQueueItem, which drives the @KafkaListener.
     * This is the bean Spring looks for to start the consumer threads.
     * @return The configured ConcurrentKafkaListenerContainerFactory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UploadQueueItem> 
        uploadQueueItemKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UploadQueueItem> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(uploadQueueItemConsumerFactory());

        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /**
     * Defines config properties specifically for the ExtractedData consumer.
     * @return The extractedData Config
     */
    public Map<String, Object> extractedDataConsumerConfig() {
        Map<String, Object> props = consumerConfig();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ExtractedData.class.getName()); 
        return props;
    }

    /**
     * Creates the ConsumerFactory for ExtractedData messages.
     * @return The configured ConsumerFactory for String keys and ExtractedData values.
     */
    @Bean
    public ConsumerFactory<String, ExtractedData> extractedDataConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            extractedDataConsumerConfig(), 
            new StringDeserializer(), 
            new JsonDeserializer<>(ExtractedData.class)
        );
    }

    /**
     * Creates the KafkaListenerContainerFactory for the ExtractedData consumer.
     * @return The configured ConcurrentKafkaListenerContainerFactory for ExtractedData.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExtractedData> extractedDataKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ExtractedData> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(extractedDataConsumerFactory());

        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

        return factory;
    }
}