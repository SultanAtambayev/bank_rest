package com.example.bankcards;

import com.example.bankcards.util.EncryptionUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public EncryptionUtil encryptionUtil() {
        return new EncryptionUtil() {
            @Override
            public String encrypt(String data) {
                // Для тестов просто добавляем префикс
                return "enc_" + data;
            }

            @Override
            public String decrypt(String encryptedData) {
                // Для тестов убираем префикс
                if (encryptedData.startsWith("enc_")) {
                    return encryptedData.substring(4);
                }
                return encryptedData;
            }
        };
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
