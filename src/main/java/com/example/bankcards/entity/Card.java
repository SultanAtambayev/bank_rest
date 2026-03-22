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

    @Column(nullable = false, unique = true)
    private String cardNumber;  // В БД хранится в зашифрованном виде

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    private LocalDate expiryDate;
    private String status;
    private BigDecimal balance;

    // Вспомогательное поле для хранения расшифрованного номера (не маппится в БД)
    @Transient
    private String plainCardNumber;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getPlainCardNumber() { return plainCardNumber; }
    public void setPlainCardNumber(String plainCardNumber) { this.plainCardNumber = plainCardNumber; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}