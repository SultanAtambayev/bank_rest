package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Администрирование", description = "Функции для администраторов")
public class AdminController {

    private final CardService cardService;
    private final UserService userService;

    public AdminController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping("/users")
    @Operation(summary = "Получить всех пользователей")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Получить пользователя по ID")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/users/{userId}/role")
    @Operation(summary = "Изменить роль пользователя")
    public User updateUserRole(@PathVariable Long userId, @RequestParam String role) {
        return userService.updateUserRole(userId, role);
    }

    @GetMapping("/cards")
    @Operation(summary = "Получить все карты (с пагинацией)")
    public Page<CardResponse> getAllCards(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return cardService.getAllCardsPaginated(pageable);
    }

    @GetMapping("/users/{userId}/cards")
    @Operation(summary = "Получить карты пользователя (админ)")
    public Page<CardResponse> getUserCards(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return cardService.getUserCardsPaginated(userId, pageable);
    }

    @PutMapping("/cards/{cardId}/activate")
    @Operation(summary = "Активировать карту")
    public CardResponse activateCard(@PathVariable Long cardId) {
        return cardService.activateCard(cardId);
    }

    @PutMapping("/cards/{cardId}/block")
    @Operation(summary = "Заблокировать карту")
    public CardResponse blockCard(@PathVariable Long cardId) {
        return cardService.blockCard(cardId);
    }

    @DeleteMapping("/cards/{cardId}")
    @Operation(summary = "Удалить карту")
    public void deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Удалить пользователя")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
