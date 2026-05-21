package com.group8.hourtracking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a project for which hours are tracked.
 */
@Document(collection = "projects")
public class Project {

    /** uuid of the project. */
    @Id
    private UUID id;

    /** uuid of project owner (user) */
    private UUID userId;

    /** Name of the project. */
    private String name;

    /** Description of the project. */
    private String description;

    /** Date and time when the project was created. */
    private LocalDateTime createdDate;

    /** Whether the project is archived. */
    private boolean archived;

    /**
     * return id.
     * @return returns id.
     */
    public UUID getId() {
        return id;
    }
    /**
     * sets Id.
     * @param id
     */
    public void setId(UUID id) {
        this.id = id;
    }
    /**
     * returns userId.
     * @return returns userId.
     */
    public UUID getUserId() {
        return userId;
    }
    /**
     * Sets userId.
     * @param userId
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    /**
     * gets project name.
     * @return returns project name.
     */
    public String getName() {
        return name;
    }
    /**
     * Sets project name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * gets project description.
     * @return return project description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * set project description.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * returns project created date.
     * @return return project created date.
     */
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    /**
     * set project created date.
     * @param createdDate 
     */
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    /**
     * returns if project is archived.
     * @return if project is archived.
     */
    public boolean isArchived() {
        return archived;
    }
    /**
     * Set is project archived?
     * @param archived
     */
    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
