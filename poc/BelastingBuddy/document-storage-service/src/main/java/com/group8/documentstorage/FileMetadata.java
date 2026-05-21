package com.group8.documentstorage;

import java.util.Date;
/**
 * class to store the file metadata for firestore storage.
 */
public class FileMetadata {
    /** file name. */
    private String fileName;
    /** file extension. */
    private String contentType;
    /** firebase url . */
    private String storageUrl;
    /** time and date of upload. */
    private Date uploadTimestamp;

    /** content of the file (encrypted ofc). */
    private String content;
    /** userID of the user whose file it is. */
    private String userID;
    /** wrappedDek stores hashed key for encryption */
    private String wrappedDek;
    /**  field used for encryption */
    private String iv;

    /** extracted amount from document (plaintext for display) */
    private String amount;
    /** extracted VAT from document (plaintext for display) */
    private String vat;

    /**
     * Default constructor for FileMetadata.
     */
    public FileMetadata() {
    }

    /**
     * class constructor.
     * @param name
     * @param extension
     * @param firebaseUrl
     * @param timeStamp
     * @param fileContent 
     * @param userConnected
     * @param keyDek
     * @param keyIV
     */
    public FileMetadata(final String name, final String extension,
        final String firebaseUrl, final Date timeStamp, final String fileContent, final String userConnected,
        final String keyDek, final String keyIV
    ) {
        this.fileName = name;
        this.contentType = extension;
        this.storageUrl = firebaseUrl;
        this.uploadTimestamp = timeStamp;
        this.content = fileContent;
        this.userID = userConnected;
        this.wrappedDek = keyDek;
        this.iv = keyIV;
    }

    /**
     * Gets the name of the file.
     * @return returns file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * sets name of the file.
     * @param name
     */
    public void setFileName(final String name) {
        this.fileName = name;
    }

    /**
     * Gets the extension of the file.
     * @return returns file extension
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * sets the extension of the file eg .pdf, .jpg etc.
     * @param extension
     */
    public void setContentType(final String extension) {
        this.contentType = extension;
    }

    /**
     * gets the url under which the file is stored in the database.
     * @return returns firebase url
     */
    public String getStorageUrl() {
        return storageUrl;
    }

    /**
     *  sets the url under which the file is stored in firebase database.
     * @param firebaseUrl
     */
    public void setStorageUrl(final String firebaseUrl) {
        this.storageUrl = firebaseUrl;
    }

    /**
     * gets the timestamp.
     * @return returns upload date
     */
    public Date getUploadTimestamp() {
        return uploadTimestamp;
    }

    /**
     * sets the time when file was uploaded.
     * @param timeStamp
     */
    public void setUploadTimestamp(final Date timeStamp) {
        this.uploadTimestamp = timeStamp;
    }

    /**
     * gets the file content.
     * @return returns content
     */
    public String getFileContent() {
        return content;
    }
    /**
     * sets the fileContent.
     * @param fileContent
     */
    public void setFileContent(final String fileContent) {
        this.content = fileContent;
    }

    /**
     * gets the userID.
     * @return returns userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * sets the userID.
     * @param userConnected
     */
    public void setUserID(final String userConnected) {
        this.userID = userConnected;
    }

    /**
     * gets the wrapped dek.
     * @return returns wrapped dek.
     */
    public String getWrappedDek() {
        return wrappedDek;
    }

    /**
     * sets the wrapped dek.
     * @param dek
     */
    public void setWrappedDek(final String dek) {
        this.wrappedDek = dek;
    }

    /**
     * gets the iv.
     * @return returns iv.
     */
    public String getVI() {
        return iv;
    }

    /**
     * sets the iv.
     * @param vIIn
     */
    public void setVI(final String vIIn) {
        this.iv = vIIn;
    }

    /**
     * gets the amount.
     * @return returns amount.
     */
    public String getAmount() {
        return amount;
    }

    /**
     * sets the amount.
     * @param amount
     */
    public void setAmount(final String amount) {
        this.amount = amount;
    }

    /**
     * gets the VAT.
     * @return returns VAT.
     */
    public String getVat() {
        return vat;
    }

    /**
     * sets the VAT.
     * @param vat
     */
    public void setVat(final String vat) {
        this.vat = vat;
    }


}
