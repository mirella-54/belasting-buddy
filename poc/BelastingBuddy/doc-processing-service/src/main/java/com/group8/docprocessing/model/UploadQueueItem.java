package com.group8.docprocessing.model;

/**
 * Represents the essential payload for the Kafka "Document Uploaded" event.
 * Contains the pointer to the raw file (GridFS ID) and necessary metadata.
 */
public class UploadQueueItem {

    // --- ESSENTIAL FIELDS FOR KAFKA EVENT PAYLOAD ---
    
    /** * The unique ID used during processing (serves as the Kafka message key for partitioning). 
     */
    private String processingId;

    /** * The GridFS unique ID/Pointer where the raw file is temporarily stored (Claim Check). 
     */
    private String gridFsId;

    /** * File name of the document (metadata for consumer logging/processing). 
     */
    private String fileName;

    /** * Mime type of the document (metadata for consumer logging/processing). 
     */
    private String contentType;

    /** * The ID of the user who uploaded the document. 
     */
    private String userId;

    // --- Constructors ---

    // Default Constructor (required by Kafka JSON deserialization)
    public UploadQueueItem() {}

    /**
     * Constructor used by the Kafka Producer (DocProcessingService).
     * @param processingId The unique processing ID.
     * @param fileName The original file name.
     * @param contentType The MIME type of the file.
     * @param gridFsId The GridFS ID where the file is stored.
     * @param userId The ID of the user who uploaded the document.
     */
    public UploadQueueItem(String processingId, String fileName, String contentType, String gridFsId, String userId) {
        this.processingId = processingId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.gridFsId = gridFsId;
        this.userId = userId;
    }


    // --- Getters and Setters ---

    public String getProcessingId() {
        return processingId;
    }

    public void setProcessingId(String processingId) {
        this.processingId = processingId;
    }

    public String getGridFsId() {
        return gridFsId;
    }

    public void setGridFsId(String gridFsId) {
        this.gridFsId = gridFsId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}