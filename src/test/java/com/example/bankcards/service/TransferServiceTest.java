package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Unit Tests")
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("ROLE_USER");

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setOwner(testUser);
        fromCard.setStatus("ACTIVE");
        fromCard.setBalance(new BigDecimal("500.00"));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setOwner(testUser);
        toCard.setStatus("ACTIVE");
        toCard.setBalance(new BigDecimal("100.00"));

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("100.00"));
    }

    @Nested
    @DisplayName("Successful Transfer Tests")
    class SuccessfulTransferTests {

        @Test
        @DisplayName("Should transfer money successfully between own cards")
        void transferBetweenOwnCards_Success() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            TransferResponse response = transferService.transferBetweenOwnCards(transferRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
            assertThat(response.getMessage()).isEqualTo("Перевод успешно выполнен");
            assertThat(fromCard.getBalance()).isEqualTo(new BigDecimal("400.00"));
            assertThat(toCard.getBalance()).isEqualTo(new BigDecimal("200.00"));
            verify(cardRepository, times(2)).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("Error Cases Tests")
    class ErrorCasesTests {

        @Test
        @DisplayName("Should throw exception when insufficient funds")
        void transfer_InsufficientFunds_ThrowsException() {
            // Given
            transferRequest.setAmount(new BigDecimal("1000.00"));

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

            // When & Then
            assertThatThrownBy(() -> transferService.transferBetweenOwnCards(transferRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Недостаточно средств на карте отправителя");
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Should throw exception when from card is blocked")
        void transfer_FromCardBlocked_ThrowsException() {
            // Given
            fromCard.setStatus("BLOCKED");

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

            // When & Then
            assertThatThrownBy(() -> transferService.transferBetweenOwnCards(transferRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Карта отправителя должна быть активна");
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Should throw exception when to card is blocked")
        void transfer_ToCardBlocked_ThrowsException() {
            // Given
            toCard.setStatus("BLOCKED");

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

            // When & Then
            assertThatThrownBy(() -> transferService.transferBetweenOwnCards(transferRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Карта получателя должна быть активна");
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void transfer_NegativeAmount_ThrowsException() {
            // Given
            transferRequest.setAmount(new BigDecimal("-50.00"));

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

            // When & Then
            assertThatThrownBy(() -> transferService.transferBetweenOwnCards(transferRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Сумма перевода должна быть положительной");
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Should throw exception when cards belong to different users")
        void transfer_DifferentOwners_ThrowsException() {
            // Given
            User otherUser = new User();
            otherUser.setId(2L);
            toCard.setOwner(otherUser);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

            // When & Then
            assertThatThrownBy(() -> transferService.transferBetweenOwnCards(transferRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Обе карты должны принадлежать вам");
            verify(cardRepository, never()).save(any(Card.class));
        }
    }
}
