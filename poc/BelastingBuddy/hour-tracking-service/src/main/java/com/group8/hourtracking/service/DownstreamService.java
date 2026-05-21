package com.group8.hourtracking.service;

import com.group8.hourtracking.configs.ServiceTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DownstreamService {
    /**
     * Service token provider for authentication.
     */
    private final ServiceTokenProvider tokenProvider;
    /**
     * REST client for making HTTP requests.
     */
    private final RestClient rest;

    public DownstreamService(ServiceTokenProvider tokenProvider, RestClient.Builder rest) {
        this.tokenProvider = tokenProvider;
        this.rest = rest.build();
    }

    public String callDocProcessing(Jwt jwt) {
        String serviceToken = tokenProvider.getAccessToken();
        String targetUrl = "http://doc-processing-service:8082/documents/internal/hourcheck";

        try {
            ResponseEntity<String> response = rest.post()
                    .uri(targetUrl)
                    .header("Authorization", "Bearer " + serviceToken)
                    .retrieve()
                    .toEntity(String.class);
            return "Downstream said: " + response.getBody();
        } catch (Exception e) {
            return   "Downstream said: " + e.getMessage();
        }
    }
}
