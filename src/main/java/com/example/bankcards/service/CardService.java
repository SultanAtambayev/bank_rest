package com.example.bankcards.service;

import com.example.bankcards.dto.CardFullResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    public CardService(CardRepository cardRepository,
                       UserRepository userRepository,
                       EncryptionUtil encryptionUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
    }

    // ==================== МЕТОДЫ ДЛЯ ШИФРОВАНИЯ ====================

    /**
     * Шифрует номер карты перед сохранением
     */
    private void encryptCardNumber(Card card) {
        if (card.getPlainCardNumber() != null && !card.getPlainCardNumber().isEmpty()) {
            card.setCardNumber(encryptionUtil.encrypt(card.getPlainCardNumber()));
        } else if (card.getCardNumber() != null && !card.getCardNumber().isEmpty()) {
            // Если передан открытый номер, шифруем его
            card.setCardNumber(encryptionUtil.encrypt(card.getCardNumber()));
        }
    }

    /**
     * Расшифровывает номер карты после чтения
     */
    private void decryptCardNumber(Card card) {
        if (card.getCardNumber() != null && !card.getCardNumber().isEmpty()) {
            try {
                String decrypted = encryptionUtil.decrypt(card.getCardNumber());
                card.setPlainCardNumber(decrypted);
            } catch (Exception e) {
                // Если не удалось расшифровать (возможно, это уже открытый текст)
                card.setPlainCardNumber(card.getCardNumber());
            }
        }
    }

    // ==================== ПОЛЬЗОВАТЕЛЬСКИЕ МЕТОДЫ ====================

    /**
     * Создание новой карты для текущего пользователя
     * Пользователь может создать карту только для себя
     */
    @Transactional
    public CardResponse createCard(CardRequest request) {
        // 1. Получаем текущего пользователя из токена
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 2. Шифруем номер карты для проверки существования
        String encryptedNumber = encryptionUtil.encrypt(request.getCardNumber());

        // 3. Проверяем, существует ли уже карта с таким номером
        if (cardRepository.existsByCardNumber(encryptedNumber)) {
            throw new RuntimeException("Карта с таким номером уже существует");
        }

        // 4. Создаем новую карту
        Card card = new Card();
        card.setPlainCardNumber(request.getCardNumber());  // Сохраняем открытый номер во временное поле
        encryptCardNumber(card);  // Шифруем для БД
        card.setOwner(currentUser);  // Владелец всегда из токена
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(request.getStatus());
        card.setBalance(request.getBalance());

        // 5. Сохраняем в базу
        card = cardRepository.save(card);

        // 6. Расшифровываем для ответа
        decryptCardNumber(card);

        // 7. Возвращаем ответ
        return new CardResponse(card);
    }

    /**
     * Получить карты пользователя (без пагинации)
     * Пользователь может видеть только свои карты
     */
    public List<Card> getUserCards(Long userId) {
        // 1. Получаем текущего пользователя из токена
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 2. Проверка прав: владелец или админ
        if (!currentUser.getId().equals(userId) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Нельзя смотреть чужие карты");
        }

        // 3. Получаем карты
        List<Card> cards = cardRepository.findByOwnerId(userId);

        // 4. Расшифровываем номера карт
        cards.forEach(this::decryptCardNumber);

        return cards;
    }

    /**
     * Получить карты пользователя с пагинацией
     */
    public Page<CardResponse> getUserCardsPaginated(Long userId, Pageable pageable) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!currentUser.getId().equals(userId) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Нельзя смотреть чужие карты");
        }

        Page<Card> cardsPage = cardRepository.findByOwnerId(userId, pageable);

        // Расшифровываем номера карт
        cardsPage.getContent().forEach(this::decryptCardNumber);

        return cardsPage.map(CardResponse::new);
    }

    /**
     * Получить карту по ID
     */
    public CardResponse getCardById(Long cardId) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        // Проверка прав: владелец карты или админ
        if (!card.getOwner().getId().equals(currentUser.getId()) &&
                !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Нельзя просматривать чужие карты");
        }

        // Расшифровываем номер карты
        decryptCardNumber(card);

        return new CardResponse(card);
    }

    /**
     * Заблокировать карту (пользователь может блокировать только свои карты)
     */
    @Transactional
    public CardResponse blockCard(Long cardId) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        // Проверка прав: владелец карты или админ
        if (!card.getOwner().getId().equals(currentUser.getId()) &&
                !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Нельзя заблокировать чужую карту");
        }

        // Проверка статуса
        if (!"ACTIVE".equals(card.getStatus())) {
            throw new RuntimeException("Можно заблокировать только активную карту");
        }

        card.setStatus("BLOCKED");
        card = cardRepository.save(card);

        // Расшифровываем номер карты
        decryptCardNumber(card);

        return new CardResponse(card);
    }

    // ==================== АДМИНИСТРАТИВНЫЕ МЕТОДЫ ====================

    /**
     * Получить все карты (только для админа) с пагинацией
     */
    public Page<CardResponse> getAllCardsPaginated(Pageable pageable) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Доступ запрещен. Только для администраторов");
        }

        Page<Card> cardsPage = cardRepository.findAll(pageable);

        // Расшифровываем номера карт
        cardsPage.getContent().forEach(this::decryptCardNumber);

        return cardsPage.map(CardResponse::new);
    }

    /**
     * Активировать карту (только для админа)
     */
    @Transactional
    public CardResponse activateCard(Long cardId) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Доступ запрещен. Только для администраторов");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        if ("ACTIVE".equals(card.getStatus())) {
            throw new RuntimeException("Карта уже активна");
        }

        if ("EXPIRED".equals(card.getStatus())) {
            throw new RuntimeException("Нельзя активировать карту с истекшим сроком действия");
        }

        card.setStatus("ACTIVE");
        card = cardRepository.save(card);

        // Расшифровываем номер карты
        decryptCardNumber(card);

        return new CardResponse(card);
    }

    /**
     * Удалить карту (только для админа)
     */
    @Transactional
    public void deleteCard(Long cardId) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Доступ запрещен. Только для администраторов");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        cardRepository.delete(card);
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Проверить существование карты по номеру (с шифрованием)
     */
    public boolean existsByCardNumber(String cardNumber) {
        String encryptedNumber = encryptionUtil.encrypt(cardNumber);
        return cardRepository.existsByCardNumber(encryptedNumber);
    }

    /**
     * Получить активные карты пользователя
     */
    public List<Card> getActiveCardsByUserId(Long userId) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!currentUser.getId().equals(userId) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Нельзя просматривать чужие карты");
        }

        List<Card> cards = cardRepository.findActiveCardsByUserId(userId);

        // Расшифровываем номера карт
        cards.forEach(this::decryptCardNumber);

        return cards;
    }

    /**
     * Проверить достаточно ли средств на карте
     */
    public boolean hasSufficientFunds(Long cardId, java.math.BigDecimal amount) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        return card.getBalance().compareTo(amount) >= 0;
    }

    /**
     * Получить карту с полным номером (только для владельца)
     */
    public CardFullResponse getCardWithFullNumber(Long cardId) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        // Только владелец может видеть полный номер
        if (!card.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Только владелец карты может видеть полный номер");
        }

        // Расшифровываем номер
        decryptCardNumber(card);

        return new CardFullResponse(card);
    }
}