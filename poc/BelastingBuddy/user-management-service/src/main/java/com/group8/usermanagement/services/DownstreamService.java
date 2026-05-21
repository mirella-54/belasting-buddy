package com.group8.usermanagement.services;

import com.group8.usermanagement.configs.ServiceTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
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
     * Calls the Document Processing Service with a service token.
     *
     * @param jwt the JWT of the calling user (not used in this method)
     * @return response from the Document Processing Service
     */
    public String callDocProcessing(Jwt jwt){
        String serviceToken = tokenProvider.getAccessToken();
        String targetUrl = "http://doc-processing-service:8082/documents/internal/stub";

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
}
