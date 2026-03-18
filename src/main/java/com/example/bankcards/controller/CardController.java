package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public CardResponse createCard(@RequestBody CardRequest request) {
        return cardService.createCard(request);
    }

    // ===== Здесь вставляем метод =====
    @GetMapping("/user/{userId}")
    public List<CardResponse> getUserCards(@PathVariable Long userId) {
        List<Card> cards = cardService.getUserCards(userId);
        return cards.stream()
                .map(CardResponse::new)
                .toList();
    }
}
