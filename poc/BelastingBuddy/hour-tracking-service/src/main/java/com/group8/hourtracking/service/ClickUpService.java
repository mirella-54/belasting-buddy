package com.group8.hourtracking.service;

import com.group8.hourtracking.dto.ClickUpTimeEntry;
import com.group8.hourtracking.dto.ClickUpTimeEntryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service for interacting with ClickUp API.
 */
@Service
public class ClickUpService {
    /** api url. */
    @Value("${clickup.api.url}") // get from application.properties
    private String apiUrl;
    /** rest template variable. */
    private final RestTemplate restTemplate;
    /** initialise clickup service.*/
    public ClickUpService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get time entries for a workspace/team within a date range.
     * By default, returns time entries from the last 30 days for the authenticated
     * user.
     * 
     * @param apiKey    the ClickUp API key
     * @param teamId    the workspace/team ID
     * @param startDate start date in Unix time (milliseconds) - optional
     * @param endDate   end date in Unix time (milliseconds) - optional
     * @param assignee  assignee user ID to filter - optional
     * @param spaceId   space ID to filter - optional
     * @param folderId  folder ID to filter - optional
     * @param listId    list ID to filter - optional
     * @param taskId    task ID to filter - optional
     * @return list of time entries
     */
    public List<ClickUpTimeEntry> getTimeEntries(String apiKey, String teamId, Long startDate,
        Long endDate, String assignee, String spaceId, String folderId, String listId, String taskId) {
        StringBuilder url = new StringBuilder(apiUrl)
            .append("/team/").append(teamId)
            .append("/time_entries");

      // Build query parameters
        StringBuilder params = new StringBuilder();
        if (startDate != null) {
            params.append("start_date=").append(startDate);
        }
        if (endDate != null) {
            if (params.length() > 0)
                params.append("&");
            params.append("end_date=").append(endDate);
        }
        if (assignee != null) {
            if (params.length() > 0)
                params.append("&");
            params.append("assignee=").append(assignee);
        }
        // Only one location filter can be included at a time
        if (spaceId != null) {
            if (params.length() > 0)
                params.append("&");
            params.append("space_id=").append(spaceId);
        } else if (folderId != null) {
            if (params.length() > 0)
                params.append("&");
            params.append("folder_id=").append(folderId);
        } else if (listId != null) {
            if (params.length() > 0)
                params.append("&");
            params.append("list_id=").append(listId);
        } else if (taskId != null) {
            if (params.length() > 0)
                params.append("&");
            params.append("task_id=").append(taskId);
        }

        if (params.length() > 0) {
            url.append("?").append(params);
        }

        HttpHeaders headers = createHeaders(apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ClickUpTimeEntryResponse> response = restTemplate.exchange(
            url.toString(),
            HttpMethod.GET,
            entity,
            ClickUpTimeEntryResponse.class);

        return response.getBody() != null ? response.getBody().getData() : null;
    }

    /**
     * headers with API key for ClickUp API authentication.
     * 
     * @param apiKey the API key
     * @return headers
     */
    private HttpHeaders createHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey); // ClickUp uses Authorization header
        headers.set("accept", "application/json");
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
