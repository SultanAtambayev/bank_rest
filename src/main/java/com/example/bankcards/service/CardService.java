package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.bankcards.dto.CardResponse;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public CardResponse createCard(CardRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Card card = new Card();
        card.setCardNumber(request.getCardNumber());
        card.setOwner(user);
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(request.getStatus());
        card.setBalance(request.getBalance());

        card = cardRepository.save(card); // сохраняем карту в БД
        return new CardResponse(card);     // возвращаем DTO с маской номера
    }

    public List<Card> getUserCards(Long userId) {
        return cardRepository.findByOwnerId(userId);
    }
}
