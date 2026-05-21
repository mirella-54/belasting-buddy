package com.group8.docprocessing.model;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents the extracted data from a user-uploaded document.
 */
@Document(collection = "extractedData")
public class ExtractedData {

    /** Unique identifier of the extracted data entry. */
    private String id;

    /** Identifier of the document this data belongs to. */
    private String documentId;

    /** The content extracted from the document. */
    private String extractedContent;

    /** The ID of the user who uploaded the document. */
    private String userId;

    public ExtractedData() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getExtractedContent() {
        return extractedContent;
    }

    public void setExtractedContent(String extractedContent) {
        this.extractedContent = extractedContent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
