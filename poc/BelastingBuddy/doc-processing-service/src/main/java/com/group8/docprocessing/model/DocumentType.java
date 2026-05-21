package com.group8.docprocessing.model;

/**
 * Enum representing the type of a document.
 */
public enum DocumentType {

    /** Document is an invoice. */
    INVOICE,

    /** Document is a receipt. */
    RECEIPT,

    /** Document is a bank statement. */
    BANK_STATEMENT,

    /** Document type is other/unknown. */
    OTHER
}
