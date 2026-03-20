package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;


@Schema(description = "Запрос на создание карты")
public class CardRequest {



    @Schema(description = "ID владельца карты", example = "1", required = true)
    private Long userId;

    @Schema(description = "Номер карты", example = "1234567812345678", required = true)
    private String cardNumber;

    @Schema(description = "Срок действия карты", example = "2026-12-31", type = "string", format = "date")
    private LocalDate expiryDate;

    @Schema(description = "Статус карты: ACTIVE, BLOCKED, EXPIRED", example = "ACTIVE", required = true)
    private String status;

    @Schema(description = "Баланс карты", example = "1000.50", required = true)
    private BigDecimal balance;

    // ===== геттеры/сеттеры =====
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
