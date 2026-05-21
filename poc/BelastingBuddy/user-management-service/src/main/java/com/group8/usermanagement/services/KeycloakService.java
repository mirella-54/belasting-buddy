package com.group8.usermanagement.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Service for interacting with Keycloak for user management tasks.
 */
@Component
public class KeycloakService {
    /**
     * RestClient for making HTTP requests to Keycloak.
     */
    private final RestClient rest;
    /**
     * Base URL of the Keycloak server.
     */
    private final String baseUrl;
    /**
     * Target realm in Keycloak.
     */
    private final String targetRealm;
    /**
     * Client ID for admin access.
     */
    private final String clientId;
    /**
     * Client secret for admin access.
     */
    private final String clientSecret;
    /**
     * Constructs a KeycloakService with the necessary configuration.
     *
     * @param restBuilder  The RestClient builder for making HTTP requests.
     * @param baseUrl      The base URL of the Keycloak server.
     * @param targetRealm  The target realm in Keycloak.
     * @param clientId     The client ID for admin access.
     * @param clientSecret The client secret for admin access.
     */
    public KeycloakService(
            RestClient.Builder restBuilder,
            @Value("${keycloak.base-url}") String baseUrl,
            @Value("${keycloak.target-realm}") String targetRealm,
            @Value("${keycloak.admin.client-id}") String clientId,
            @Value("${keycloak.admin.client-secret}") String clientSecret
    ) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.targetRealm = targetRealm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.rest = restBuilder.baseUrl(this.baseUrl).build();
    }
    /**
     * Obtain admin access token using client credentials grant.
     *
     * @return Authorization header value with Bearer token.
     */
    private String adminAuthHeader() {
        String tokenUrl = baseUrl + "/realms/" + targetRealm + "/protocol/openid-connect/token";

        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        try {
            Map<?, ?> resp = rest.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            Object token = resp == null ? null : resp.get("access_token");
            if (token == null) {
                throw new IllegalStateException("No access_token in service token response for client " + clientId);
            }
            return "Bearer " + token;
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "Failed to obtain service token (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        }
    }

    /**
     * Creates a new user in Keycloak with the specified details.
     *
     * @param username  The username for the new user.
     * @param email     The email address for the new user.
     * @param password  The password for the new user.
     * @param firstName The first name of the new user.
     * @param lastName  The last name of the new user.
     * @return The ID of the newly created user.
     * @throws IllegalStateException if the user already exists or creation fails.
     */
    public String createUser(String username, String email, String password, String firstName, String lastName) {
        final String auth = adminAuthHeader();
        final String usersUrl = baseUrl + "/admin/realms/" + targetRealm + "/users";

        // 1) Fail fast if exists
        String searchUrl = usersUrl + "?username=" + username;
        List<?> existing = rest.get()
                .uri(searchUrl)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .body(List.class);
        if (existing != null && !existing.isEmpty()) {
            throw new IllegalStateException("User '" + username + "' already exists in realm '" + targetRealm + "'");
        }

        // 2) Create user (with name fields)
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("username", username);
        createPayload.put("email", email);
        createPayload.put("firstName", firstName);
        createPayload.put("lastName", lastName);
        createPayload.put("enabled", true);
        createPayload.put("emailVerified", true);
        createPayload.put("requiredActions", List.of());

        URI location;
        try {
            ResponseEntity<Void> resp = rest.post()
                    .uri(usersUrl)
                    .header(HttpHeaders.AUTHORIZATION, auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createPayload)
                    .retrieve()
                    .toBodilessEntity();

            location = resp.getHeaders().getLocation();
            if (location == null) {
                throw new IllegalStateException("User was created but no Location header returned.");
            }
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Create user failed: "+e.getStatusCode()+" "+e.getResponseBodyAsString(),e);
        }

        String userId = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);

        // 3) Set permanent password
        Map<String, Object> cred = Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );
        rest.put()
                .uri(usersUrl + "/" + userId + "/reset-password")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cred)
                .retrieve()
                .toBodilessEntity();

        // 4) Double-clear required actions
        rest.put()
                .uri(usersUrl + "/" + userId)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("requiredActions", List.of()))
                .retrieve()
                .toBodilessEntity();

        return userId;
    }


    /**
     * Logs in a user with the provided credentials.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A map containing the login response, including access token.
     * @throws IllegalStateException if login fails.
     */
    public Map<String, Object> loginUser(String username, String password) {
        String tokenUrl = baseUrl + "/realms/" + targetRealm + "/protocol/openid-connect/token";

        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret); // confidential client authenticates here
        form.add("username", username);
        form.add("password", password);

        try {
            Map<?, ?> resp = rest.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            if (resp == null || resp.get("access_token") == null) {
                throw new IllegalStateException("No access_token in login response for user " + username);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) resp;
            return cast;
        } catch (RestClientResponseException e) {
            // Bubble up the exact Keycloak error to help debugging (invalid_grant, etc.)
            throw new IllegalStateException(
                    "Failed user login (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        }
    }
}
