package com.group8.documentstorage;

import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.EncryptResponse;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.protobuf.ByteString;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.util.Base64;

public class CryptoHelper {

    /** AES key size. */
    private static final int AES_KEY_SIZE = 256;

    /** IV length. */
    private static final int GCM_IV_LENGTH = 12;

    /** Tag length */
    private static final int GCM_TAG_LENGTH = 128;

    /** encrypt content function is used to encrypt the string which is the content of the file.
     * @param plaintext text of the file. 
     * @param kmsClient google cloud key management service client. 
     * @param kmsKeyName name of the kms key set up in google cloud.
     * @return returns the encryption parts in a class.
     */
    public static EncryptedContent encryptContent(
        String plaintext, KeyManagementServiceClient kmsClient,
        CryptoKeyName kmsKeyName) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        SecretKey dek = keyGen.generateKey();
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, dek, spec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        EncryptResponse resp = kmsClient.encrypt(kmsKeyName, ByteString.copyFrom(dek.getEncoded()));
        byte[] wrappedDek = resp.getCiphertext().toByteArray();
        return new EncryptedContent(
            Base64.getEncoder().encodeToString(ciphertext),
            Base64.getEncoder().encodeToString(iv),
            Base64.getEncoder().encodeToString(wrappedDek)
        );
    }

    /**
     * function to decrypt content of the file back to readable string.
     * @param enc  ciphertext, IV and wrapped dek.
     * @param kmsClient google cloud key management service client information.
     * @param kmsKeyName name of the key set up in google cloud.
     * @return returns the text of the file.
     */
    public static String decryptContent(EncryptedContent enc, KeyManagementServiceClient kmsClient,
        CryptoKeyName kmsKeyName) throws Exception {
        byte[] wrappedDek = Base64.getDecoder().decode(enc.wrappedDek);
        DecryptResponse resp = kmsClient.decrypt(kmsKeyName, ByteString.copyFrom(wrappedDek));
        byte[] dekBytes = resp.getPlaintext().toByteArray();
        SecretKey dek = new SecretKeySpec(dekBytes, "AES");
        byte[] iv = Base64.getDecoder().decode(enc.iv);
        byte[] ciphertext = Base64.getDecoder().decode(enc.ciphertext);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, dek, spec);
        byte[] plaintextBytes = cipher.doFinal(ciphertext);

        return new String(plaintextBytes);
    }
}
