package com.group8.hourtracking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a log entry for third-party sync operations.
 */
@Document(collection = "syncLogs")
public class SyncLog {

    /** uuid of the sync log entry. */
    @Id
    private UUID id;

    /**
     * uuid of the third-party account connection. (impl. might change in the
     * future)
     */
    private UUID accountId;

    /** uuid of the user who initiated the sync. */
    private UUID userId;

    /** name of the provider (e.g., "clockify"). */
    private String provider;

    /** date and time when the sync was initiated. */
    private LocalDateTime syncDate;

    /** status of the sync operation. */
    private SyncStatus status;

    /** number of time entries imported during this sync. */
    private int entriesImported;

    /** error message if sync failed. */
    private String errorMessage;

    /**
     *  get id.
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
     * get accountId.
     * @return accountId.
     */
    public UUID getAccountId() {
        return accountId;
    }
    /**
     * set account id.
     * @param accountId
     */
    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
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
     * get sync date.
     * @return sync date.
     */
    public LocalDateTime getSyncDate() {
        return syncDate;
    }
    /**
     * set sync date.
     * @param syncDate
     */
    public void setSyncDate(LocalDateTime syncDate) {
        this.syncDate = syncDate;
    }
    /**
     * get sync status.
     * @return status.
     */
    public SyncStatus getStatus() {
        return status;
    }
    /**
     * set sync status.
     * @param status
     */
    public void setStatus(SyncStatus status) {
        this.status = status;
    }
    /**
     * get imported entries.
     * @return imported entries.
     */
    public int getEntriesImported() {
        return entriesImported;
    }
    /**
     * set imported entries.
     * @param entriesImported
     */
    public void setEntriesImported(int entriesImported) {
        this.entriesImported = entriesImported;
    }
    /**
     * get error msg.
     * @return error msg.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    /**
     * set error msg.
     * @param errorMessage 
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
