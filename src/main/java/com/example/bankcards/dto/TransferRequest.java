package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Запрос на перевод между картами")
public class TransferRequest {

    @Schema(description = "ID карты отправителя", example = "1", required = true)
    @NotNull(message = "ID карты отправителя обязателен")
    private Long fromCardId;

    @Schema(description = "ID карты получателя", example = "2", required = true)
    @NotNull(message = "ID карты получателя обязателен")
    private Long toCardId;

    @Schema(description = "Сумма перевода", example = "100.50", required = true)
    @NotNull(message = "Сумма перевода обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    // Геттеры и сеттеры
    public Long getFromCardId() {
        return fromCardId;
    }

    public void setFromCardId(Long fromCardId) {
        this.fromCardId = fromCardId;
    }

    public Long getToCardId() {
        return toCardId;
    }

    public void setToCardId(Long toCardId) {
        this.toCardId = toCardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
