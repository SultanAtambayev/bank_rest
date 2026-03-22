package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Unit Tests")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private User adminUser;
    private Card testCard;
    private CardRequest cardRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("ROLE_USER");

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole("ROLE_ADMIN");

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("enc_1234567812345678");
        testCard.setPlainCardNumber("1234567812345678");
        testCard.setOwner(testUser);
        testCard.setExpiryDate(LocalDate.of(2026, 12, 31));
        testCard.setStatus("ACTIVE");
        testCard.setBalance(new BigDecimal("1000.00"));

        cardRequest = new CardRequest();
        cardRequest.setCardNumber("1234567812345678");
        cardRequest.setExpiryDate(LocalDate.of(2026, 12, 31));
        cardRequest.setStatus("ACTIVE");
        cardRequest.setBalance(new BigDecimal("1000.00"));
    }

    @Nested
    @DisplayName("Create Card Tests")
    class CreateCardTests {

        @Test
        @DisplayName("Should create card successfully for authenticated user")
        void createCard_Success() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(encryptionUtil.encrypt(cardRequest.getCardNumber())).thenReturn("enc_1234567812345678");
            when(cardRepository.existsByCardNumber("enc_1234567812345678")).thenReturn(false);
            when(cardRepository.save(any(Card.class))).thenReturn(testCard);
            when(encryptionUtil.decrypt(any())).thenReturn(cardRequest.getCardNumber());

            // When
            CardResponse response = cardService.createCard(cardRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getOwnerId()).isEqualTo(testUser.getId());
            assertThat(response.getBalance()).isEqualTo(new BigDecimal("1000.00"));
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("Should throw exception when card number already exists")
        void createCard_AlreadyExists_ThrowsException() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(encryptionUtil.encrypt(cardRequest.getCardNumber())).thenReturn("enc_1234567812345678");
            when(cardRepository.existsByCardNumber("enc_1234567812345678")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> cardService.createCard(cardRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Карта с таким номером уже существует");
            verify(cardRepository, never()).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("Get User Cards Tests")
    class GetUserCardsTests {

        @Test
        @DisplayName("Should return user cards for owner")
        void getUserCards_Owner_Success() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findByOwnerId(1L)).thenReturn(List.of(testCard));
            when(encryptionUtil.decrypt(any())).thenReturn("1234567812345678");

            // When
            List<Card> cards = cardService.getUserCards(1L);

            // Then
            assertThat(cards).hasSize(1);
            assertThat(cards.get(0).getOwner().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Should throw exception when user tries to view another user's cards")
        void getUserCards_Unauthorized_ThrowsException() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("otheruser");
            SecurityContextHolder.setContext(securityContext);
            User otherUser = new User();
            otherUser.setId(3L);
            otherUser.setUsername("otheruser");
            otherUser.setRole("ROLE_USER");
            when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

            // When & Then
            assertThatThrownBy(() -> cardService.getUserCards(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Нельзя смотреть чужие карты");
            verify(cardRepository, never()).findByOwnerId(any());
        }

        @Test
        @DisplayName("Should return user cards for admin")
        void getUserCards_Admin_Success() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(cardRepository.findByOwnerId(1L)).thenReturn(List.of(testCard));
            when(encryptionUtil.decrypt(any())).thenReturn("1234567812345678");

            // When
            List<Card> cards = cardService.getUserCards(1L);

            // Then
            assertThat(cards).hasSize(1);
            assertThat(cards.get(0).getOwner().getId()).isEqualTo(testUser.getId());
        }
    }

    @Nested
    @DisplayName("Block Card Tests")
    class BlockCardTests {

        @Test
        @DisplayName("Should block card successfully for owner")
        void blockCard_Owner_Success() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
            when(cardRepository.save(any(Card.class))).thenReturn(testCard);
            when(encryptionUtil.decrypt(any())).thenReturn("1234567812345678");

            // When
            CardResponse response = cardService.blockCard(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("BLOCKED");
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("Should throw exception when blocking already blocked card")
        void blockCard_AlreadyBlocked_ThrowsException() {
            // Given
            testCard.setStatus("BLOCKED");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

            // When & Then
            assertThatThrownBy(() -> cardService.blockCard(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Можно заблокировать только активную карту");
            verify(cardRepository, never()).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("Admin Methods Tests")
    class AdminMethodsTests {

        @Test
        @DisplayName("Should get all cards for admin")
        void getAllCardsPaginated_Admin_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Card> cardPage = new PageImpl<>(List.of(testCard));

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(cardRepository.findAll(pageable)).thenReturn(cardPage);
            when(encryptionUtil.decrypt(any())).thenReturn("1234567812345678");

            // When
            Page<CardResponse> result = cardService.getAllCardsPaginated(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to get all cards")
        void getAllCardsPaginated_NonAdmin_ThrowsException() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> cardService.getAllCardsPaginated(pageable))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Доступ запрещен. Только для администраторов");
            verify(cardRepository, never()).findAll(org.mockito.ArgumentMatchers.any(Pageable.class));
        }
    }
}
