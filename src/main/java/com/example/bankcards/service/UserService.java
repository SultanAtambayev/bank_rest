package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Метод для регистрации нового пользователя
    public User registerUser(User user) {
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }

    // Метод для поиска пользователя по имени
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Метод для поиска пользователя по email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
