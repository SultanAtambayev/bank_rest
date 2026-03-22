package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardResponse {

    private Long id;
    private String cardNumber;   // Маскированный номер карты
    private Long ownerId;
    private LocalDate expiryDate;
    private String status;
    private BigDecimal balance;

    /**
     * Конструктор для преобразования из сущности Card
     * Использует расшифрованный номер карты из plainCardNumber
     */
    public CardResponse(Card card) {
        this.id = card.getId();

        // Используем расшифрованный номер для маскирования
        // Приоритет: plainCardNumber (расшифрованный) > cardNumber (зашифрованный в БД)
        String cardNumberForMask = card.getPlainCardNumber();
        if (cardNumberForMask == null || cardNumberForMask.isEmpty()) {
            // Если по какой-то причине plainCardNumber пустой, пробуем расшифровать
            // (на случай, если decryptCardNumber не был вызван)
            cardNumberForMask = card.getCardNumber();
        }

        this.cardNumber = maskCardNumber(cardNumberForMask);
        this.ownerId = card.getOwner().getId();
        this.expiryDate = card.getExpiryDate();
        this.status = card.getStatus();
        this.balance = card.getBalance();
    }

    /**
     * Маскируем номер карты: **** **** **** 1234
     * @param cardNumber номер карты (открытый текст)
     * @return замаскированный номер
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "****";
        }

        // Удаляем возможные пробелы
        String cleanNumber = cardNumber.replaceAll("\\s+", "");

        if (cleanNumber.length() < 4) {
            return "****";
        }

        String last4 = cleanNumber.substring(cleanNumber.length() - 4);
        return "**** **** **** " + last4;
    }

    // ===== Геттеры =====
    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    // ===== Сеттеры (на случай, если понадобятся) =====
    public void setId(Long id) {
        this.id = id;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}