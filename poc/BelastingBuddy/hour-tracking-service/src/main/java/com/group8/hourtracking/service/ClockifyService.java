package com.group8.hourtracking.service;

import com.group8.hourtracking.dto.ClockifyTimeEntry;
import com.group8.hourtracking.dto.ClockifyUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for interacting with Clockify API.
 */
@Service
public class ClockifyService {


    /** clockify api url */
    @Value("${clockify.api.url}") // get from application.properties
    private String apiUrl;
    /** variable for the rest template */
    private final RestTemplate restTemplate;

    /** initiate ClockifyService */
    public ClockifyService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get current user info from Clockify.
     * 
     * @param apiKey the Clockify API key
     * @return Clockify user
     */
    public ClockifyUser getCurrentUser(String apiKey) {
        String url = apiUrl + "/user";
        HttpHeaders headers = createHeaders(apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ClockifyUser> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            ClockifyUser.class);

        return response.getBody();
    }

    /**
     * Get time entries for a user in a workspace.
     * 
     * @param apiKey      the Clockify API key
     * @param workspaceId the workspace ID
     * @param userId      the user ID
     * @param start       start date (optional)
     * @param end         end date (optional)
     * @return list of time entries
     */
    public List<ClockifyTimeEntry> getTimeEntries(String apiKey, String workspaceId, String userId,
        LocalDate start, LocalDate end) {
        StringBuilder url = new StringBuilder(apiUrl)
            .append("/workspaces/").append(workspaceId)
            .append("/user/").append(userId)
            .append("/time-entries");

        // optional date filters
        boolean hasParams = false;
        if (start != null) {
            url.append("?start=").append(start).append("T00:00:00Z");
            hasParams = true;
        }
        if (end != null) {
            url.append(hasParams ? "&" : "?").append("end=").append(end).append("T23:59:59Z");
        }

        HttpHeaders headers = createHeaders(apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ClockifyTimeEntry>> response = restTemplate.exchange(
            url.toString(),
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<ClockifyTimeEntry>>() {
            });

        return response.getBody();
    }

    /**
     * Create HTTP headers with API key that can be used to auth for clockify API.
     * 
     * @param apiKey the API key
     * @return headers
     */
    private HttpHeaders createHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
