package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CardFullResponse {

    private Long id;
    private String cardNumber;      // Полный номер (без маски)
    private Long ownerId;
    private LocalDate expiryDate;
    private String status;
    private BigDecimal balance;

    public CardFullResponse(Card card) {
        this.id = card.getId();
        // Используем расшифрованный полный номер
        this.cardNumber = card.getPlainCardNumber() != null ?
                card.getPlainCardNumber() : card.getCardNumber();
        this.ownerId = card.getOwner().getId();
        this.expiryDate = card.getExpiryDate();
        this.status = card.getStatus();
        this.balance = card.getBalance();
    }

    // Геттеры
    public Long getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public Long getOwnerId() { return ownerId; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    public BigDecimal getBalance() { return balance; }
}
