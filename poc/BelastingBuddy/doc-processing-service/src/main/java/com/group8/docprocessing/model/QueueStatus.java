package com.group8.docprocessing.model;

/**
 * Enum representing the status of a document in the processing queue.
 */
public enum QueueStatus {

    /** The document is queued and waiting to be processed. */
    QUEUED,

    /** The document is currently being processed. */
    PROCESSING,

    /** The document has been successfully processed. */
    COMPLETED,

    /** Processing the document has failed. */
    FAILED

}
