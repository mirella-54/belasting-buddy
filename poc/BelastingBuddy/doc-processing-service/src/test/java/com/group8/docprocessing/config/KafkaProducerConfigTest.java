package com.group8.docprocessing.config;

import com.group8.docprocessing.model.ExtractedData;
import com.group8.docprocessing.model.UploadQueueItem;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = KafkaProducerConfig.class)
@TestPropertySource(properties = {
    "SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092"
})
class KafkaProducerConfigTest {

    /**
     * The expected Kafka bootstrap servers for testing purposes.
     */
    private static final String EXPECTED_BOOTSTRAP_SERVERS = "localhost:9092";

    /**
     * The Kafka producer configuration used for testing.
     */
    @Autowired
    private KafkaProducerConfig kafkaProducerConfig;

    /**
     * The producer factory for handling UploadQueueItem messages.
     */
    @Autowired
    private ProducerFactory<String, UploadQueueItem> producerFactory;

    /**
     * The Kafka template for handling UploadQueueItem messages.
     */
    @Autowired
    private KafkaTemplate<String, UploadQueueItem> kafkaTemplate;

    /**
     * The producer factory for handling ExtractedData messages.
     */
    @Autowired
    private ProducerFactory<String, ExtractedData> extractedDataProducerFactory;

    /**
     * The Kafka template for handling ExtractedData messages.
     */
    @Autowired
    private KafkaTemplate<String, ExtractedData> extractedDataKafkaTemplate;

    private void assertCommonProducerConfig(Map<String, Object> config) {
        assertThat(config).containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, EXPECTED_BOOTSTRAP_SERVERS);
        assertThat(config).containsEntry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        assertThat(config).containsEntry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        assertThat(config).containsEntry(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
    }

    @Test
    void producerFactoryShouldBeInitializedAndConfiguredCorrectly() {
        assertNotNull(producerFactory);
        Map<String, Object> config = producerFactory.getConfigurationProperties();
        assertCommonProducerConfig(config);
    }

    @Test
    void kafkaTemplateShouldBeInitializedWithCorrectFactory() {
        assertNotNull(kafkaTemplate);
        assertThat(kafkaTemplate.getProducerFactory()).isSameAs(producerFactory);
    }

    @Test
    void extractedDataProducerFactoryShouldBeInitializedAndConfiguredCorrectly() {
        assertNotNull(extractedDataProducerFactory);
        Map<String, Object> config = extractedDataProducerFactory.getConfigurationProperties();
        assertCommonProducerConfig(config);
    }

    @Test
    void extractedDataKafkaTemplateShouldBeInitializedWithCorrectFactory() {
        assertNotNull(extractedDataKafkaTemplate);
        assertThat(extractedDataKafkaTemplate.getProducerFactory()).isSameAs(extractedDataProducerFactory);
    }
}