package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Номер карты (будем потом шифровать)
    @Column(nullable = false, unique = true)
    private String cardNumber;

    // Владелец
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    // Срок действия
    private LocalDate expiryDate;

    // Статус
    private String status; // ACTIVE, BLOCKED, EXPIRED

    // Баланс
    private BigDecimal balance;

    // ===== геттеры/сеттеры =====
    public Long getId() { return id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
