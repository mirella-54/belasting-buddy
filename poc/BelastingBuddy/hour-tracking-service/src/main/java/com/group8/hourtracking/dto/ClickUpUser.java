package com.group8.hourtracking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for ClickUp user response (from their api)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickUpUser {
    /** id. */
    private Long id;
    /** username */
    private String username;
    /** email */
    private String email;
    /** color */
    private String color;
    /** initials */
    private String initials;
    /** profile picture */
    private String profilePicture;
    /** timezone */
    private String timezone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
