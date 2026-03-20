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

    // Конструктор для преобразования из сущности Card
    public CardResponse(Card card) {
        this.id = card.getId();
        this.cardNumber = maskCardNumber(card.getCardNumber());
        this.ownerId = card.getOwner().getId();
        this.expiryDate = card.getExpiryDate();
        this.status = card.getStatus();
        this.balance = card.getBalance();
    }

    // Маскируем номер карты: **** **** **** 1234
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 4) return "****";
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }

    // ===== геттеры =====
    public Long getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public Long getOwnerId() { return ownerId; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    public BigDecimal getBalance() { return balance; }
}
