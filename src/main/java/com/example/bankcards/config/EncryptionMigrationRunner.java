package com.example.bankcards.config;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.EncryptionUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EncryptionMigrationRunner implements CommandLineRunner {

    private final CardRepository cardRepository;
    private final EncryptionUtil encryptionUtil;

    public EncryptionMigrationRunner(CardRepository cardRepository, EncryptionUtil encryptionUtil) {
        this.cardRepository = cardRepository;
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== Проверка необходимости шифрования карт ===");

        boolean needsEncryption = false;

        for (Card card : cardRepository.findAll()) {
            String cardNumber = card.getCardNumber();

            // Проверяем, не зашифрован ли уже номер
            // Если номер начинается с "enc:" или похож на Base64, считаем зашифрованным
            if (cardNumber != null && !isProbablyEncrypted(cardNumber)) {
                needsEncryption = true;
                System.out.println("Найдена незашифрованная карта ID: " + card.getId() +
                        ", номер: " + maskForLog(cardNumber));

                // Шифруем номер
                String encrypted = encryptionUtil.encrypt(cardNumber);
                card.setCardNumber(encrypted);
                cardRepository.save(card);
                System.out.println("Зашифрована карта ID: " + card.getId());
            }
        }

        if (!needsEncryption) {
            System.out.println("Все карты уже зашифрованы");
        }

        System.out.println("=== Проверка шифрования завершена ===");
    }

    /**
     * Проверяет, похож ли номер на зашифрованный (Base64)
     */
    private boolean isProbablyEncrypted(String value) {
        // Зашифрованные значения обычно содержат только Base64 символы
        return value.matches("^[A-Za-z0-9+/=]+$") && value.length() > 20;
    }

    /**
     * Маскирует номер для лога (показывает только последние 4 цифры)
     */
    private String maskForLog(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
