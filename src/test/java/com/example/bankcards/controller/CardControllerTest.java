package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CardController Integration Tests")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String userToken;
    private Long userId;
    private Long cardId;

    @BeforeEach
    void setUp() {
        // Очищаем базу
        cardRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("ROLE_USER");
        user = userRepository.save(user);
        userId = user.getId();

        // Создаем тестовую карту
        Card card = new Card();
        card.setCardNumber("enc_1234567812345678");
        card.setPlainCardNumber("1234567812345678");
        card.setOwner(user);
        card.setExpiryDate(LocalDate.of(2026, 12, 31));
        card.setStatus("ACTIVE");
        card.setBalance(new BigDecimal("1000.00"));
        card = cardRepository.save(card);
        cardId = card.getId();

        // Генерируем JWT токен
        userToken = jwtUtils.generateToken("testuser", "ROLE_USER");
    }

    @Test
    @DisplayName("Should create card successfully")
    void createCard_Success() throws Exception {
        CardRequest request = new CardRequest();
        request.setCardNumber("9876543210987654");
        request.setExpiryDate(LocalDate.of(2027, 12, 31));
        request.setStatus("ACTIVE");
        request.setBalance(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(userId))
                .andExpect(jsonPath("$.balance").value(500.00))
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 7654"));
    }

    @Test
    @DisplayName("Should get user cards with pagination")
    void getUserCardsPaginated_Success() throws Exception {
        mockMvc.perform(get("/api/cards/user/{userId}", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should get card by ID")
    void getCardById_Success() throws Exception {
        mockMvc.perform(get("/api/cards/{cardId}", cardId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.ownerId").value(userId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should block card successfully")
    void blockCard_Success() throws Exception {
        mockMvc.perform(put("/api/cards/{cardId}/block", cardId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @DisplayName("Should return 403 when no token provided")
    void createCard_Unauthorized_Returns403() throws Exception {
        CardRequest request = new CardRequest();
        request.setCardNumber("1111111111111111");
        request.setExpiryDate(LocalDate.of(2026, 12, 31));
        request.setStatus("ACTIVE");
        request.setBalance(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());  // ← изменили на isForbidden()
    }
}
