package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    @Value("${encryption.key}")
    private String encryptionKey;

    private SecretKey getSecretKey() {
        // Используем 16-байтный ключ для AES-128
        byte[] keyBytes = encryptionKey.getBytes();
        // Если ключ длиннее 16 байт, обрезаем, если короче - дополняем
        byte[] fixedKeyBytes = new byte[16];
        System.arraycopy(keyBytes, 0, fixedKeyBytes, 0, Math.min(keyBytes.length, 16));
        return new SecretKeySpec(fixedKeyBytes, ALGORITHM);
    }

    public String encrypt(String data) {
        try {
            if (data == null) return null;
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования данных", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null) return null;
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка дешифрования данных", e);
        }
    }
}
