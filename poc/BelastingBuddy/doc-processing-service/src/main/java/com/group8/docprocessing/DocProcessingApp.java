package com.group8.docprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.client.RestTemplate; 

@SpringBootApplication
@EnableKafka
public class DocProcessingApp {
    
    public static void main(String[] args) {
        SpringApplication.run(DocProcessingApp.class, args);
    }

    /**
     * Defines the RestTemplate bean required for the DocumentStorageClient.
     * The DocumentStorageClient needs this bean for making HTTP calls.
     * @return A new instance of RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}