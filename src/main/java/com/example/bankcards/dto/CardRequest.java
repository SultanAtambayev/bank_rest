package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Future;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Запрос на создание карты")
public class CardRequest {

    @Schema(description = "Номер карты", example = "1234567812345678", required = true)
    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать 16 цифр")
    private String cardNumber;

    @Schema(description = "Срок действия карты", example = "2026-12-31", type = "string", format = "date", required = true)
    @NotNull(message = "Срок действия обязателен")
    @Future(message = "Срок действия должен быть в будущем")
    private LocalDate expiryDate;

    @Schema(description = "Статус карты: ACTIVE, BLOCKED, EXPIRED", example = "ACTIVE", required = true)
    @NotBlank(message = "Статус обязателен")
    @Pattern(regexp = "ACTIVE|BLOCKED|EXPIRED", message = "Статус должен быть: ACTIVE, BLOCKED или EXPIRED")
    private String status;

    @Schema(description = "Баланс карты", example = "1000.50", required = true)
    @NotNull(message = "Баланс обязателен")
    @Positive(message = "Баланс должен быть положительным")
    private BigDecimal balance;

    // ===== Геттеры и сеттеры =====
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}