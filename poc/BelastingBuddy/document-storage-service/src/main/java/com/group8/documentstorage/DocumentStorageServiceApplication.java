package com.group8.documentstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



/**
 * set up of the microservice to start it up.
 */
@SpringBootApplication
public final class DocumentStorageServiceApplication {
    /**
     * constructor for the class to shush the maven warning.
     */
    private DocumentStorageServiceApplication() {
    }
    /**
     * Main class of the microservice.
     * @param args Arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(DocumentStorageServiceApplication.class, args);
    }
}
