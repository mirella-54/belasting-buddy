package com.group8.docprocessing.service;

import com.group8.docprocessing.config.ServiceTokenProvider;
import com.group8.docprocessing.model.ExtractedData;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Service to handle communication with downstream services.
 */
@Service
public class DownstreamService {
    /**
     * Provider for service-to-service authentication tokens.
     */
    private final ServiceTokenProvider tokenProvider;
    /**
     * REST client for making HTTP requests.
     */
    private final RestClient rest;
    /**
     * Constructs the DownstreamService with required dependencies.
     *
     * @param tokenProvider provider for service tokens
     * @param rest REST client builder
     */
    public DownstreamService(ServiceTokenProvider tokenProvider, RestClient.Builder rest) {
        this.tokenProvider = tokenProvider;
        this.rest = rest.build();
    }
    /**
     * Calls the Hour Tracking Service with a service token.
     *
     * @param jwt the JWT of the calling user (not used in this method)
     * @return response from the Hour Tracking Service
     */
    public String callHourService(Jwt jwt){
        String serviceToken = tokenProvider.getAccessToken();
        String targetUrl = "http://hour-tracking-service:8084/hours/internal/hourresponse";

        try {
            ResponseEntity<String> response = rest.post()
                    .uri(targetUrl)
                    .header("Authorization", "Bearer " + serviceToken)
                    .retrieve()
                    .toEntity(String.class);
            return "Downstream said: " + response.getBody();
        } catch (Exception e) {
            return  "Downstream said: " + e.getMessage();
        }
    }

    /**
     * Calls the Document Storage Service /upload or /upload-local endpoint.
     * This method secures the call using a service-to-service Bearer token.
     *
     * @param eventData The extracted data metadata.
     * @param fileData The byte array of the file content.
     * @param fileName The original file name.
     * @param endpoint The specific endpoint path (e.g., "/upload" or "/upload-local").
     * @return The response entity from the document storage service.
     */
    public ResponseEntity<String> sendExtractedDataToStorage(
            ExtractedData eventData, 
            byte[] fileData, 
            String fileName, 
            String endpoint) {
        
        String serviceToken = tokenProvider.getAccessToken();
        String targetUrl = "http://doc-storage-service:8083/api/files-archive" + endpoint;

        ByteArrayResource fileResource = new ByteArrayResource(fileData) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        body.add("file", fileResource); 
        body.add("content", eventData.getExtractedContent());
        body.add("userID", eventData.getUserId());
        
        try {
            ResponseEntity<String> response = rest.post()
                    .uri(targetUrl)
                    .header("Authorization", "Bearer " + serviceToken)
                    .contentType(MediaType.MULTIPART_FORM_DATA) 
                    .body(body) 
                    .retrieve()
                    .toEntity(String.class);

            return response;

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to call Document Storage Service: " +
                e.getMessage());
        }
    }
}
