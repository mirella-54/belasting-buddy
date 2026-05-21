package com.group8.hourtracking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO for ClickUp time entries response wrapper (from their api)
 * ClickUp wraps time entries in a "data" field
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickUpTimeEntryResponse {
    /** data. */
    private List<ClickUpTimeEntry> data;
    /**
     * get data.
     * @return data.
     */
    public List<ClickUpTimeEntry> getData() {
        return data;
    }
    /**
     * set data.
     * @param data
     */
    public void setData(List<ClickUpTimeEntry> data) {
        this.data = data;
    }
}
