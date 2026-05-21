package com.group8.docprocessing.config;

import com.group8.docprocessing.model.ExtractedData;
import com.group8.docprocessing.model.UploadQueueItem;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = KafkaConsumerConfig.class)
@TestPropertySource(properties = {
    "SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092",
    "SPRING_KAFKA_CONSUMER_GROUP_ID=test-group"
})
class KafkaConsumerConfigTest {

    private static final String EXPECTED_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String EXPECTED_GROUP_ID = "test-group";

    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;

    @Autowired
    private ConsumerFactory<String, UploadQueueItem> uploadQueueConsumerFactory;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, UploadQueueItem> uploadQueueKafkaListenerContainerFactory;

    @Autowired
    private ConsumerFactory<String, ExtractedData> extractedDataConsumerFactory;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, ExtractedData> extractedDataKafkaListenerContainerFactory;

    // ----------------------------------------------------------------------------------
    // Tests for UploadQueueItem Configuration
    // ----------------------------------------------------------------------------------

    @Test
    void uploadQueueConsumerFactoryShouldBeInitializedAndConfiguredCorrectly() {
        assertNotNull(uploadQueueConsumerFactory, "UploadQueueConsumerFactory bean should not be null.");

        Map<String, Object> factoryConfigs = uploadQueueConsumerFactory.getConfigurationProperties();
        assertThat(factoryConfigs).isNotEmpty();

        assertThat(factoryConfigs).containsEntry(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, EXPECTED_BOOTSTRAP_SERVERS
        );

        assertTrue(
            factoryConfigs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG).equals(JsonDeserializer.class),
            "UploadQueueConsumerFactory should use JsonDeserializer for values."
        );
    }

    @Test
    void uploadQueueKafkaListenerContainerFactoryShouldBeInitializedWithCorrectFactory() {
        assertNotNull(uploadQueueKafkaListenerContainerFactory, "UploadQueueContainerFactory bean should not be null.");

        assertNotNull(
            uploadQueueKafkaListenerContainerFactory.getConsumerFactory(),
            "ContainerFactory must have a ConsumerFactory set."
        );
        
        assertThat(uploadQueueKafkaListenerContainerFactory.getConsumerFactory()).isSameAs(uploadQueueConsumerFactory);
    }

    // ----------------------------------------------------------------------------------
    // Tests for ExtractedData Configuration
    // ----------------------------------------------------------------------------------

    @Test
    void extractedDataConsumerFactoryShouldBeInitializedAndConfiguredCorrectly() {
        assertNotNull(extractedDataConsumerFactory, "ExtractedDataConsumerFactory bean should not be null.");

        Map<String, Object> factoryConfigs = extractedDataConsumerFactory.getConfigurationProperties();
        assertThat(factoryConfigs).isNotEmpty();
        
        assertThat(factoryConfigs).containsEntry(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, EXPECTED_BOOTSTRAP_SERVERS
        );
        
        // Check deserialization classes
        assertTrue(
            factoryConfigs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG).equals(JsonDeserializer.class),
            "ExtractedDataConsumerFactory should use JsonDeserializer for values."
        );
    }

    @Test
    void extractedDataKafkaListenerContainerFactoryShouldBeInitializedWithCorrectFactory() {
        assertNotNull(extractedDataKafkaListenerContainerFactory, "ExtractedDataContainerFactory bean should not be null.");

        assertNotNull(
            extractedDataKafkaListenerContainerFactory.getConsumerFactory(),
            "ContainerFactory must have a ConsumerFactory set."
        );

        assertThat(extractedDataKafkaListenerContainerFactory.getConsumerFactory()).isSameAs(extractedDataConsumerFactory);
    }
}