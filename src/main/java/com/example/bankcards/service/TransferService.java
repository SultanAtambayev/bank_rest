package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public TransferService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransferResponse transferBetweenOwnCards(TransferRequest request) {
        // 1. Получаем текущего пользователя из токена
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 2. Находим карты
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new RuntimeException("Карта отправителя не найдена"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new RuntimeException("Карта получателя не найдена"));

        // 3. Проверяем, что обе карты принадлежат текущему пользователю
        if (!fromCard.getOwner().getId().equals(currentUser.getId()) ||
                !toCard.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Обе карты должны принадлежать вам");
        }

        // 4. Проверяем статус карт
        if (!"ACTIVE".equals(fromCard.getStatus())) {
            throw new RuntimeException("Карта отправителя должна быть активна");
        }

        if (!"ACTIVE".equals(toCard.getStatus())) {
            throw new RuntimeException("Карта получателя должна быть активна");
        }

        // 5. Проверяем достаточность средств
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Недостаточно средств на карте отправителя");
        }

        // 6. Проверяем положительную сумму
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма перевода должна быть положительной");
        }

        // 7. Выполняем перевод
        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // 8. Формируем ответ
        return new TransferResponse(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount(),
                "SUCCESS",
                "Перевод успешно выполнен"
        );
    }
}
