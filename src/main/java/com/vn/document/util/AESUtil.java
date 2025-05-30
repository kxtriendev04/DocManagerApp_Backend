package com.vn.document.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class AESUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16; // Kích thước IV là 16 bytes cho AES

    public static SecretKeySpec generateKeyFromPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] encrypt(byte[] data, String password) throws Exception {
        SecretKeySpec key = generateKeyFromPassword(password);
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedData = cipher.doFinal(data);

        // Nối IV vào đầu dữ liệu mã hóa
        byte[] result = new byte[IV_SIZE + encryptedData.length];
        System.arraycopy(iv, 0, result, 0, IV_SIZE);
        System.arraycopy(encryptedData, 0, result, IV_SIZE, encryptedData.length);
        return result;
    }

    public static byte[] decrypt(byte[] encryptedData, String password) throws Exception {
        SecretKeySpec key = generateKeyFromPassword(password);

        // Tách IV từ dữ liệu mã hóa
        byte[] iv = new byte[IV_SIZE];
        byte[] actualEncryptedData = new byte[encryptedData.length - IV_SIZE];
        System.arraycopy(encryptedData, 0, iv, 0, IV_SIZE);
        System.arraycopy(encryptedData, IV_SIZE, actualEncryptedData, 0, actualEncryptedData.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(actualEncryptedData);
    }
}