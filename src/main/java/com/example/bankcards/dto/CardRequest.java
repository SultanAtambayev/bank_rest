package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardRequest {

    private Long userId;           // Владелец карты
    private String cardNumber;     // Номер карты
    private LocalDate expiryDate;  // Срок действия
    private String status;         // Статус: ACTIVE, BLOCKED, EXPIRED
    private BigDecimal balance;    // Баланс

    // ===== геттеры и сеттеры =====
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
