package com.group8.documentstorage;

public class EncryptedContent {
    /** encrypted text of the file  */
    public final String ciphertext;
    /** iv associated with the file content and the key */
    public final String iv;
    /** encrypted dek key */
    public final String wrappedDek;

    /** constructor for the EncryptedContent class.
     * @param ciphertext - encrypted file content.
     * @param iv bytes added to encryption.
     * @param wrappedDek Dek key encrypted with key from kms.
     */
    public EncryptedContent(String ciphertext, String iv, String wrappedDek) {
        this.ciphertext = ciphertext;
        this.iv = iv;
        this.wrappedDek = wrappedDek;
    }
}