package com.group8.hourtracking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for Clockify user response (from their api)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClockifyUser {
    /** id. */
    private String id;
    /** user email. */
    private String email;
    /** user name. */
    private String name;
    /** default workspace. */
    private String defaultWorkspace;
    /**
     * get id.
     * @return id.
     */
    public String getId() {
        return id;
    }
    /**
     * set id.
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * get email.
     * @return email.
     */
    public String getEmail() {
        return email;
    }
    /**
     * set email.
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * get name.
     * @return name.
     */
    public String getName() {
        return name;
    }
    /**
     * set name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * get default workspace.
     * @return default workspace.
     */
    public String getDefaultWorkspace() {
        return defaultWorkspace;
    }
    /**
     * set default workspace.
     * @param defaultWorkspace
     */
    public void setDefaultWorkspace(String defaultWorkspace) {
        this.defaultWorkspace = defaultWorkspace;
    }
}
