package com.group8.docprocessing.model;

public class FileMetadata {

    /**
     * The unique identifier for the file, used for retrieval and management.
     */
    private String fileId;

    public FileMetadata(String fileId) {
        this.fileId = fileId;
    }

    public FileMetadata() {}
    
    // Getters and setters
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}