package com.group8.hourtracking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a connected third party productivity tool account (like
 * clockify/clikcup/...)
 */
@Document(collection = "thirdPartyAccounts")
public class ThirdPartyAccount {

    /** uuid of the account connection. */
    @Id
    private UUID id;

    /** uuid of the user who owns this connection. */
    private UUID userId;

    /** name of the provider (e.g., Clockify, ClickUp). */
    private String provider;

    /** encrypted authentication token for the third-party service. */
    private String authToken;

    /** Date and time when the connection was established. */
    private LocalDateTime connectedDate;

    /** Date and time of the last successful sync. */
    private LocalDateTime lastSyncDate;

    /** Whether the connection is currently active. */
    private boolean active;

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
     * get userId.
     * @return userId.
     */
    public UUID getUserId() {
        return userId;
    }
    /**
     * set userId.
     * @param userId
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    /**
     * get provider.
     * @return provider.
     */
    public String getProvider() {
        return provider;
    }
    /**
     * set provider.
     * @param provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }
    /**
     * get auth token.
     * @return auth token.
     */
    public String getAuthToken() {
        return authToken;
    }
    /**
     * set auth token.
     * @param authToken
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    /**
     * get connected time.
     * @return connected time.
     */
    public LocalDateTime getConnectedDate() {
        return connectedDate;
    }
    /**
     * set connected time.
     * @param connectedDate
     */
    public void setConnectedDate(LocalDateTime connectedDate) {
        this.connectedDate = connectedDate;
    }
    /**
     * get last sync time.
     * @return last sync time.
     */
    public LocalDateTime getLastSyncDate() {
        return lastSyncDate;
    }
    /**
     *  set last sync time.
     * @param lastSyncDate
     */
    public void setLastSyncDate(LocalDateTime lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }
    /**
     * check if acc is archived.
     * @return is archived.
     */
    public boolean isActive() {
        return active;
    }
    /**
     * set acc is archived.
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
