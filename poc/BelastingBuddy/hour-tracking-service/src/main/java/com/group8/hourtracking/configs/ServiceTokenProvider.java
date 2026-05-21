package com.group8.hourtracking.configs;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Provider for service-to-service authentication tokens.
 */
@Component
public class ServiceTokenProvider {
    /**
     * REST client for making HTTP requests.
     */
    private final RestClient rest;
    /**
     * Token endpoint URL.
     */
    private final String tokenUrl;
    /**
     * Client ID for authentication.
     */
    private final String clientId;
    /**
     * Client secret for authentication.
     */
    private final String clientSecret;
    /**
     * Constructs the ServiceTokenProvider with required dependencies.
     *
     * @param restBuilder REST client builder
     * @param tokenUrl token endpoint URL
     * @param clientId client ID
     * @param clientSecret client secret
     */
    public ServiceTokenProvider(
            RestClient.Builder restBuilder,
            @Value("${app.keycloak.services.token-url}") String tokenUrl,
            @Value("${app.keycloak.services.client-id}") String clientId,
            @Value("${app.keycloak.services.client-secret}") String clientSecret
    ) {
        this.rest = restBuilder.build();
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
    /**
     * Retrieves an access token using client credentials.
     *
     * @return access token string
     */
    public String getAccessToken() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<?, ?> resp = rest.post()
                .uri(tokenUrl)
                .body(form)
                .retrieve()
                .body(Map.class);

        return (String) resp.get("access_token");
    }

    /**
     * Exchanges a user token for a service token (on-behalf-of the user).
     *
     * @param userToken the JWT of the calling user
     * @return a new access token representing both the service and user context
     */
    public String exchangeUserToken(String userToken) {
        WebClient client = WebClient.create();

        return client.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("subject_token", userToken)
                        .with("requested_token_type", "urn:ietf:params:oauth:token-type:access_token"))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("access_token").asText())
                .block();
    }
}
