package com.yellowforktech.littlefamilytree.remote;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A class to perform password-based AES encryption and decryption in CBC mode.
 * 128, 192, and 256-bit encryption are supported, provided that the latter two
 * are permitted by the Java runtime's jurisdiction policy files.
 * <br/>
 * The public interface for this class consists of the static methods
 * {@link #encrypt} and {@link #decrypt}, which encrypt and decrypt arbitrary
 * streams of data, respectively.
 */
public class AES {

    // AES specification - changing will break existing encrypted streams!
    private static final String CIPHER_SPEC = "AES/CBC/PKCS5Padding";

    // Key derivation specification - changing will break existing streams!
    private static final String KEYGEN_SPEC = "PBKDF2WithHmacSHA1";
    private static final int SALT_LENGTH = 16; // in bytes
    private static final int AUTH_KEY_LENGTH = 8; // in bytes
    private static final int ITERATIONS = 32768;

    // Process input/output streams in chunks - arbitrary
    private static final int BUFFER_SIZE = 1024;

    private static void getIvBytes(byte[] iv, String ivKey) {
        byte[] keyBytes = ivKey.getBytes();
        for(int b=0; b<iv.length; b++) {
            if (keyBytes.length>b) {
                iv[b] = keyBytes[b];
            } else {
                iv[b] = 0;
            }
        }
    }

    public static String encrypt(int keyLength, String ivKey, String strDataToEncrypt)
            throws Exception {
        byte[] iv = new byte[keyLength / 8];
        getIvBytes(iv, ivKey);
        SecretKeySpec secretKey = new SecretKeySpec(iv, "AES");

        Cipher aesCipherForEncryption = Cipher.getInstance("AES/CBC/PKCS7PADDING"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!

        aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKey,
                new IvParameterSpec(iv));

        byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
        byte[] byteCipherText = aesCipherForEncryption.doFinal(byteDataToEncrypt);
        String strCipherText = Base64.encodeToString(byteCipherText, Base64.NO_WRAP);
        return strCipherText;
    }

    public static String decrypt(int keyLength, String ivKey, String strDataToDecrypt)
            throws Exception {

        byte[] iv = new byte[keyLength / 8];
        getIvBytes(iv, ivKey);
        SecretKeySpec secretKey = new SecretKeySpec(iv, "AES");

        Cipher aesCipherForDecryption = Cipher.getInstance("AES/CBC/PKCS7PADDING"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!
        aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKey,
                new IvParameterSpec(iv));
        byte[] byteDataToDecrypt = Base64.decode(strDataToDecrypt, Base64.NO_WRAP);
        //byte[] byteDataToDecrypt = strDataToDecrypt.getBytes();
        byte[] byteDecryptedText = aesCipherForDecryption.doFinal(byteDataToDecrypt);
        String strDecryptedText = new String(byteDecryptedText);
        return strDecryptedText;
    }
}