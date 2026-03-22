package com.example.bankcards.controller;

import com.example.bankcards.service.CardService;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardFullResponse;  // ← ДОБАВИТЬ ЭТОТ ИМПОРТ
import com.example.bankcards.entity.Card;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Карты", description = "Управление банковскими картами")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @Operation(summary = "Создание новой карты для текущего пользователя")
    public CardResponse createCard(@Valid @RequestBody CardRequest request) {
        return cardService.createCard(request);
    }

    // ===== ПРОСТОЙ МЕТОД БЕЗ ПАГИНАЦИИ (для удобного просмотра) =====
    @GetMapping("/user/{userId}/all")
    @Operation(summary = "Получить ВСЕ карты пользователя (без пагинации)")
    public List<CardResponse> getAllUserCards(@PathVariable Long userId) {
        List<Card> cards = cardService.getUserCards(userId);
        return cards.stream()
                .map(CardResponse::new)
                .toList();
    }

    // ===== МЕТОД С ПАГИНАЦИЕЙ (для больших списков) =====
    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить карты пользователя (с пагинацией)")
    public Page<CardResponse> getUserCardsPaginated(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return cardService.getUserCardsPaginated(userId, pageable);
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Получить карту по ID")
    public CardResponse getCardById(@PathVariable Long cardId) {
        return cardService.getCardById(cardId);
    }

    @PutMapping("/{cardId}/block")
    @Operation(summary = "Заблокировать карту")
    public CardResponse blockCard(@PathVariable Long cardId) {
        return cardService.blockCard(cardId);
    }

    @GetMapping("/{cardId}/full")
    @Operation(summary = "Получить карту с полным номером (только для владельца)")
    public CardFullResponse getCardWithFullNumber(@PathVariable Long cardId) {
        return cardService.getCardWithFullNumber(cardId);
    }
}