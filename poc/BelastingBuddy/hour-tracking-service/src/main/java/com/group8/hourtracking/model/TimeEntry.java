package com.group8.hourtracking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a time entry for a specific project (and date).
 */
@Document(collection = "timeEntries")
public class TimeEntry {

    /** uuid of the time entry. */
    @Id
    private UUID id;

    /** uuid of the project this entry belongs to. */
    private UUID projectId;

    /** date for which hours were logged. */
    private LocalDate date;

    /** number of minutes worked. */
    private int minutes;

    /** Optional description or notes for this entry. */
    private String description;

    /**
     * Whether this entry was manually entered or synced from a third-party tool.
     */
    private EntrySource source; // might remove this in the future

    /**
     * The provider/source of this entry (for example "manual", "clickup",
     * "clockify", ...).
     * Used to support multiple entries per day from different sources (SAME day).
     */
    private String sourceProvider;

    /**
     * The external ID from the third-party tool (e.g., ClickUp entry ID).
     * Null for manual entries.
     */
    private String externalId;

    /** Date and time when the entry was created. */
    private LocalDateTime createdDate;

    /** Date and time when the entry was last modified. */
    private LocalDateTime modifiedDate;
    /**
     * get id.
     * @return id.
     */
    public UUID getId() {
        return id;
    }
    /**
     * set id.
     * @param id
     */
    public void setId(UUID id) {
        this.id = id;
    }
    /**
     * get projectId.
     * @return projectId.
     */
    public UUID getProjectId() {
        return projectId;
    }
    /**
     * set projectId.
     * @param projectId
     */
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
    /**
     * get date.
     * @return date.
     */
    public LocalDate getDate() {
        return date;
    }
    /**
     * set date.
     * @param date
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }
    /**
     * get minutes.
     * @return minutes.
     */
    public int getMinutes() {
        return minutes;
    }
    /**
     * set minutes.
     * @param minutes
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
    /**
     * get description.
     * @return description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * set description.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * get source.
     * @return source.
     */
    public EntrySource getSource() {
        return source;
    }
    /**
     * set source provider.
     * @param source
     */
    public void setSource(EntrySource source) {
        this.source = source;
    }
    /**
     * get source provider.
     * @return source provider.
     */
    public String getSourceProvider() {
        return sourceProvider;
    }
    /**
     * set source provider.
     * @param sourceProvider
     */
    public void setSourceProvider(String sourceProvider) {
        this.sourceProvider = sourceProvider;
    }
    /**
     * get external id.
     * @return external id.
     */
    public String getExternalId() {
        return externalId;
    }
    /**
     * set external id.
     * @param externalId
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    /**
     * get created date.
     * @return created date.
     */
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    /**
     * set createdDate.
     * @param createdDate
     */
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    /**
     * get modified date.
     * @return modified date.
     */
    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }
    /**
     * set modified date.
     * @param modifiedDate
     */
    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
