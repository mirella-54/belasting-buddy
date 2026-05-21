package com.group8.hourtracking.dto;

/**
 * DTO for sync operation result.
 */
public class SyncResult {
    /** provider. */
    private String provider;
    /** entries synched. */
    private int entriesSynced;
    /** status. */
    private String status;
    /** message. */
    private String message;

    public SyncResult(String provider, int entriesSynced, String status, String message) {
        this.provider = provider;
        this.entriesSynced = entriesSynced;
        this.status = status;
        this.message = message;
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
     * get entries.
     * @return entries.
     */
    public int getEntriesSynced() {
        return entriesSynced;
    }
    /**
     * set entries synced.
     * @param entriesSynced
     */
    public void setEntriesSynced(int entriesSynced) {
        this.entriesSynced = entriesSynced;
    }
    /**
     * get status.
     * @return status.
     */
    public String getStatus() {
        return status;
    }
    /**
     * set status.
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }
    /**
     * get msg.
     * @return msg.
     */
    public String getMessage() {
        return message;
    }
    /**
     * set msg.
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
