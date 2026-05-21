package com.group8.docprocessing.model;

/**
 * Enum representing the overall status of a document.
 */
public enum Status {

    /** The document has been uploaded but not yet processed. */
    UPLOADED,

    /** The document is currently being processed. */
    PROCESSING,

    /** The document has been successfully processed. */
    PROCESSED,

    /** An error occurred during processing of the document. */
    ERROR

}
