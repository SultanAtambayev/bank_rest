package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ответ на перевод")
public class TransferResponse {

    @Schema(description = "ID перевода")
    private Long transferId;

    @Schema(description = "ID карты отправителя")
    private Long fromCardId;

    @Schema(description = "ID карты получателя")
    private Long toCardId;

    @Schema(description = "Сумма перевода")
    private BigDecimal amount;

    @Schema(description = "Статус перевода")
    private String status;

    @Schema(description = "Время перевода")
    private LocalDateTime timestamp;

    @Schema(description = "Сообщение")
    private String message;

    public TransferResponse() {}

    public TransferResponse(Long fromCardId, Long toCardId, BigDecimal amount, String status, String message) {
        this.transferId = System.currentTimeMillis();
        this.fromCardId = fromCardId;
        this.toCardId = toCardId;
        this.amount = amount;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    // Геттеры
    public Long getTransferId() {
        return transferId;
    }

    public Long getFromCardId() {
        return fromCardId;
    }

    public Long getToCardId() {
        return toCardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
